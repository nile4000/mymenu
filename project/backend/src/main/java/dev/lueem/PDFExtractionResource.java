package dev.lueem;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParsingException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import dev.lueem.ai.OpenAiClient;
import dev.lueem.extract.PDFLayoutTextStripper;

@Path("/api")
public class PDFExtractionResource {

    @Inject
    OpenAiClient openAiClient;

    private static final Logger LOGGER = Logger.getLogger(PDFExtractionResource.class.getName());

    private static final String PDF_TEMP_PREFIX = "uploaded";
    private static final String PDF_TEMP_SUFFIX = ".pdf";
    private static final String FILE_REASON_HEADER = "Reason";

    // state your prompt here
    private static final String QUESTION_PREFIX = "Extract articles and return a list in a valid JSON format: Name, Price, Quantity, Discount (or 0 if none) from the given receipt.\n";

    private Response createBadRequestResponse(String reason) {
        return Response.status(Status.BAD_REQUEST).header(FILE_REASON_HEADER, reason).build();
    }

    private String cleanUpContent(String content) {
        StringBuilder cleaned = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    cleaned.append(line.trim()).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error cleaning content", e);
        }
        return cleaned.toString().trim();
    }

    private static String extractArticlesUntilTotal(String receipt) {
        int indexOfTotal = receipt.indexOf("Total CHF");
        if (indexOfTotal != -1) {
            return receipt.substring(0, indexOfTotal).trim();
        }
        return receipt;
    }

    public static String extractTotal(String receipt) {
        String pattern = "Total CHF (\\d+\\.\\d{2})";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(receipt);

        if (m.find()) {
            return m.group(1);
        } else {
            return "0.00";
        }
    }

    private static String extractDate(String text) {
        String datumRegex = "\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b";
        Pattern pattern = Pattern.compile(datumRegex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(0);
        }
        // fallback to current date
        return getTimestamp();
    }

    static String getText(File pdfFile) {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            return new PDFLayoutTextStripper().getText(doc);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading PDF", e);
            return null;
        }
    }

    private JsonArray getAnswerOpenAI(String extractedText) {
        String questionPrefix = QUESTION_PREFIX;

        // Combining the question prefix with the actual content extracted from the PDF
        String fullQuestion = questionPrefix + extractedText;
        return openAiClient.askQuestion(fullQuestion);
    }

    private static String getTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(formatter);
        return timestamp;
    }

    private File extractPdfFromMultipart(MultipartFormDataInput input) {
        Map<String, Collection<FormValue>> map = input.getValues();
        File pdfFile = null;
        for (var entry : map.entrySet()) {
            for (FormValue value : entry.getValue()) {
                if (value.isFileItem() && value.getFileName().endsWith(PDF_TEMP_SUFFIX)) {
                    String timestamp = getTimestamp();
                    String fileName = PDF_TEMP_PREFIX + "_" + timestamp + ".pdf";
                    try {
                        File tempFile = File.createTempFile(fileName, PDF_TEMP_SUFFIX);
                        tempFile.deleteOnExit(); // Ensure the file is deleted when the JVM exits
                        try (InputStream inputStream = value.getFileItem().getInputStream()) {
                            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            pdfFile = tempFile;
                            return pdfFile; // Return the extracted PDF file
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE, "Error copying file", e);
                            throw new RuntimeException("Error copying file", e);
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Error creating temp file", e);
                        throw new RuntimeException("Error creating temp file", e);
                    }
                }
            }
        }
        return pdfFile;
    }

    @POST
    @Path("extract")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractArticlesFromPdf(MultipartFormDataInput input) {

        try {
            // extract pdf from multipart
            File pdfFile = extractPdfFromMultipart(input);

            if (pdfFile == null) {
                return createBadRequestResponse("No PDF file found in the request");
            }

            // extract text
            String documentContent = getText(pdfFile);
            String cleanedContent = cleanUpContent(documentContent);
            String extractTotal = extractTotal(cleanedContent);
            String extractDate = extractDate(cleanedContent);
            String cuttedEnd = extractArticlesUntilTotal(cleanedContent);

            // testing purpose
            // System.out.println("ext");
            // System.out.println(cleanedContent);
            // JsonArray articlesJson = null;

            // ask openai
            JsonArray articlesJson = getAnswerOpenAI(cuttedEnd);

            try {
                UUID uid = UUID.randomUUID();
                // ToDo: read out Corp name
                JsonObject jsonResponse = Json.createObjectBuilder()
                        .add("UID", uid.toString())
                        .add("PurchaseDate", extractDate)
                        .add("Corp", "Coop")
                        .add("Total", extractTotal)
                        .add("Articles", articlesJson)
                        .build();
                return Response.ok(jsonResponse)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON +
                                ";charset=UTF-8")
                        .build();

            } catch (JsonParsingException e) {
                LOGGER.log(Level.SEVERE, "JSON parsing failed", e);
                return Response.ok("failed").header(HttpHeaders.CONTENT_TYPE,
                        MediaType.TEXT_PLAIN + ";charset=UTF-8")
                        .build();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "General Error in extracting Articles from PDF", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}
