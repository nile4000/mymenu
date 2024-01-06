package dev.lueem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;


import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParsingException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

// import dev.lueem.ai.OpenAiClient;
// import dev.lueem.extract.ExtractCoopPosition;
// import dev.lueem.extract.ExtractPositionDetail;
import dev.lueem.extract.PDFLayoutTextStripper;
import dev.lueem.writer.JSONFileWriter;

@Path("/rest")
public class PDFExtractionResource {

    private static final String PDF_TEMP_PREFIX = "uploadedPdf";
    private static final String PDF_TEMP_SUFFIX = ".pdf";
    private static final String FILE_REASON_HEADER = "Reason";
    private static final String NO_PDF_UPLOADED_MSG = "No PDF file was uploaded (use form parameter 'pdfFile')";

    // ExtractCoopPosition extractLine = new ExtractCoopPosition();
    // ExtractPositionDetail articleDetail = new ExtractPositionDetail();
    JSONFileWriter jsonFileWriter = new JSONFileWriter();

    // @POST
    // @Path("/extract")
    // @Consumes(MediaType.MULTIPART_FORM_DATA)
    // @Produces(MediaType.APPLICATION_OCTET_STREAM)
    // public Response extractTextFromPdf(@MultipartForm FormData formData) {
    //     if (formData.getPdfFile() == null) {
    //         return createBadRequestResponse(NO_PDF_UPLOADED_MSG);
    //     }

    //     File pdfFile = convertInputStreamToFile(formData.getPdfFile());
    //     String text = getText(pdfFile);

    //     LocalDate localDate = extractCreationDateFromPdf(pdfFile);
    //     File temp = createTempFileWithText(text);

    //     return Response.ok(temp, MediaType.APPLICATION_OCTET_STREAM)
    //             .header("Content-Disposition", "attachment;filename=" + localDate + "_content_extracted.txt")
    //             .build();
    // }

    private Response createBadRequestResponse(String reason) {
        return Response.status(Status.BAD_REQUEST).header(FILE_REASON_HEADER, reason).build();
    }

    private File convertInputStreamToFile(InputStream inputStream) {
        File tempFile;
        try {
            tempFile = File.createTempFile(PDF_TEMP_PREFIX, PDF_TEMP_SUFFIX);
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to process the uploaded file", e);
        }
        return tempFile;
    }

    // private LocalDate extractCreationDateFromPdf(File pdfFile) {
    //     try (PDDocument doc = Loader.loadPDF(pdfFile)) {
    //         Calendar creationDate = doc.getDocumentInformation().getCreationDate();
    //         return LocalDateTime.ofInstant(creationDate.toInstant(), creationDate.getTimeZone().toZoneId())
    //                 .toLocalDate();
    //     } catch (IOException e) {
    //         throw new RuntimeException("Error extracting creation date from the PDF", e);
    //     }
    // }

    // private File createTempFileWithText(String text) {
    //     try {
    //         File temp = File.createTempFile("tempfile", ".tmp");
    //         try (PrintStream out = new PrintStream(new FileOutputStream(temp))) {
    //             out.print(text);
    //         }
    //         return temp;
    //     } catch (IOException e) {
    //         throw new RuntimeException("Error creating temp file", e);
    //     }
    // }

    @POST
    @Path("/article")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractArticlesFromPdf(@MultipartForm FormData formData) throws BadRequestException, IOException {
        if (formData.getPdfFile() == null) {
            return createBadRequestResponse(NO_PDF_UPLOADED_MSG);
        }

        File pdfFile = convertInputStreamToFile(formData.getPdfFile());
        String documentContent = getText(pdfFile);
        String cleanedContent = cleanContent(documentContent);
        String cuttedEnd = extractArticlesUntilTotal(cleanedContent);

        // OpenAiClient openAiClient = new OpenAiClient();
        String questionText = "Extract articles and return a list in a valid JSON format: name, price, quantity, discount (or 0 if none) from the given receipt.\n";
        String questionPDFString = cuttedEnd;
        String question = questionText + questionPDFString;
        String answer = "hello";//openAiClient.askQuestion(question);

        // System.out.println("Question: " + answer);

        JsonObject jsonResponse;
        try {
            // Try to parse the answer string into a JsonObject
            JsonReader jsonReader = Json.createReader(new StringReader(answer));
            JsonObject answerJson = jsonReader.readObject();
            jsonReader.close();

            jsonResponse = Json.createObjectBuilder().add("answer", answerJson).build();
            return Response.ok(jsonResponse)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        } catch (JsonParsingException e) {
            // If parsing fails, return the original answer as plain text
            return Response.ok(answer).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN + ";charset=UTF-8")
                    .build();
        }

    }

    private String cleanContent(String content) {
        String cleaned = content.replaceAll(" +", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s+$", "");
        cleaned = cleaned.replaceAll("\n+", "\n");
        return cleaned.trim();
    }

    private static String extractArticlesUntilTotal(String receipt) {
        int indexOfTotal = receipt.indexOf("Total CHF");
        if (indexOfTotal != -1) {
            return receipt.substring(0, indexOfTotal).trim();
        }
        return receipt; // Rückgabe des ursprünglichen Belegs, wenn "Total CHF" nicht gefunden wird
    }

    static String getText(File pdfFile) {
        try (PDDocument doc = Loader.loadPDF(pdfFile)) {
            // Dokumenteninhalt wird übergeben, Textformatierung & Layout erstellung
            return new PDFLayoutTextStripper().getText(doc);
        } catch (IOException e) {
            e.printStackTrace(); // Oder eine andere geeignete Fehlerbehandlung
        }
        return null;
    }

}
