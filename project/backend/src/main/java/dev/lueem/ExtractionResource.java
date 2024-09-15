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

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import dev.lueem.ai.OpenAiClient;
import dev.lueem.extract.PDFLayoutTextStripper;
import dev.lueem.extract.TextUtils;

@Path("/api")
public class ExtractionResource {

    @Inject
    OpenAiClient openAiClient;

    @Inject
    TextUtils textUtils;

    private static final Logger LOGGER = Logger.getLogger(ExtractionResource.class.getName());
    private static final String PDF_TEMP_PREFIX = "uploaded";
    private static final String PDF_TEMP_SUFFIX = ".pdf";
    private static final String FILE_REASON_HEADER = "Reason";
    // state your prompt here
    private static final String QUESTION_PREFIX = "Extract articles and return a list in a valid JSON format: Name, Price, Quantity, Discount (or 0 if none) from the given receipt.\n";

    private Response createBadRequestResponse(String reason) {
        return Response.status(Status.BAD_REQUEST).header(FILE_REASON_HEADER, reason).build();
    }

    static String getTextPDF(File pdfFile) {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            return new PDFLayoutTextStripper().getText(doc);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading PDF", e);
            return null;
        }
    }

    static String getTextImage(File imageFile) {
        ITesseract tesseract = new Tesseract();
        try {
            // load data for German
            tesseract.setDatapath("../../../../../test/resources/test-tessdata/deu.traineddata");
            String result = tesseract.doOCR(imageFile);
            return result;
        } catch (TesseractException e) {
            LOGGER.log(Level.SEVERE, "Error processing image with Tesseract", e);
            return null;
        }
    }

    private JsonArray getAnswerOpenAI(String extractedText) {
        String questionPrefix = QUESTION_PREFIX;
        // Combining the question prefix with the actual content extracted from the PDF
        String fullQuestion = questionPrefix + extractedText;
        return openAiClient.askQuestion(fullQuestion, "article");
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
                            return pdfFile;
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
            File pdfFile = extractPdfFromMultipart(input);

            if (pdfFile == null) {
                return createBadRequestResponse("No PDF file found in the request");
            }

            // extract text
            // ToDo: check if the file is a PDF or an image
            String documentContent = getTextPDF(pdfFile);
            if (documentContent == null) {
                // try to extract text from image
                documentContent = getTextImage(pdfFile);
            }
            String cleanedContent = textUtils.cleanUpContent(documentContent);
            String extractTotal = textUtils.extractTotal(cleanedContent);
            String extractDate = textUtils.extractDate(cleanedContent);
            String cuttedEnd = textUtils.extractArticlesUntilTotal(cleanedContent);

            // testing purpose
            // System.out.println("ext");
            // System.out.println(cleanedContent);
            // JsonArrayBuilder articlesJson = Json.createArrayBuilder();
            // articlesJson.add(Json.createObjectBuilder()
            // .add("Name", "Test")).build();

            // ask openai
            JsonArray articlesJson = getAnswerOpenAI(cuttedEnd);

            try {
                String corp = textUtils.extractCorp(cleanedContent);
                UUID uid = UUID.randomUUID();
                JsonObject jsonResponse = Json.createObjectBuilder()
                        .add("UID", uid.toString())
                        .add("PurchaseDate", extractDate)
                        .add("Corp", corp)
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
