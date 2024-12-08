package dev.lueem.util;

import dev.lueem.extract.PDFLayoutTextStripper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    private static final Logger LOGGER = Logger.getLogger(FileUtils.class.getName());
    private static final String PDF_TEMP_PREFIX = "uploaded";
    private static final String PDF_TEMP_SUFFIX = ".pdf";

    /**
     * Extracts a PDF file from multipart form data input and saves it as a
     * temporary file.
     *
     * @param input Multipart form data input containing the uploaded files.
     * @return The temporary PDF file if found, otherwise null.
     */
    public static File extractPdfFromMultipart(MultipartFormDataInput input) {
        return input.getValues().values().stream()
                .flatMap(Collection::stream)
                .filter(FormValue::isFileItem)
                .filter(formValue -> formValue.getFileName().endsWith(PDF_TEMP_SUFFIX))
                .findFirst()
                .map(FileUtils::saveToTempFile)
                .orElse(null);
    }

    /**
     * Extracts text from the given file based on its type.
     *
     * @param file The file from which to extract text.
     * @return The extracted text, or null if extraction failed or file type is
     *         unsupported.
     */
    public static String getTextFromFile(File file) {
        if (file.getName().endsWith(PDF_TEMP_SUFFIX)) {
            return extractTextFromPDF(file);
        }
        LOGGER.warning("Unsupported file type for text extraction: " + file.getName());
        return null;
    }

    /**
     * Saves the uploaded form value to a temporary file.
     *
     * @param formValue The form value representing the uploaded file.
     * @return The temporary file if successful, otherwise null.
     */
    private static File saveToTempFile(FormValue formValue) {
        String uniqueFileName = String.format("%s_%s%s", PDF_TEMP_PREFIX, UUID.randomUUID(), PDF_TEMP_SUFFIX);
        try {
            File tempFile = File.createTempFile(uniqueFileName, PDF_TEMP_SUFFIX);
            tempFile.deleteOnExit();
            try (InputStream inputStream = formValue.getFileItem().getInputStream()) {
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Saved uploaded PDF to temporary file: " + tempFile.getAbsolutePath());
                return tempFile;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save uploaded PDF to temporary file.", e);
            return null;
        }
    }

    /**
     * Extracts text content from a PDF file.
     *
     * @param pdfFile The PDF file from which to extract text.
     * @return The extracted text, or null if extraction failed.
     */
    private static String extractTextFromPDF(File pdfFile) {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFLayoutTextStripper stripper = new PDFLayoutTextStripper();
            String text = stripper.getText(document);
            LOGGER.info("Successfully extracted text from PDF.");
            return text;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error extracting text from PDF: " + e.getMessage());
            return null;
        }
    }
}
