package dev.lueem.util

import dev.lueem.extract.PDFLayoutTextStripper
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import org.apache.pdfbox.Loader
import org.jboss.resteasy.reactive.server.multipart.FormValue
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput

object PdfFileHandler {
    private val LOGGER = Logger.getLogger(PdfFileHandler::class.java.name)
    private const val PDF_TEMP_PREFIX = "uploaded"
    private const val PDF_TEMP_SUFFIX = ".pdf"

    /**
     * Extracts a PDF file from multipart form data input and saves it as a temporary file.
     *
     * @param input Multipart form data input containing the uploaded files.
     * @return The temporary PDF file if found, otherwise null.
     */
    fun extractPdfFromMultipart(input: MultipartFormDataInput): File? {
        return input.values
                .values
                .stream()
                .flatMap { it.stream() }
                .filter { it.isFileItem }
                .filter { formValue -> formValue.fileName.endsWith(PDF_TEMP_SUFFIX) }
                .findFirst()
                .map { saveToTempFile(it) }
                .orElse(null)
    }

    /**
     * Extracts text from the given file based on its type.
     *
     * @param file The file from which to extract text.
     * @return The extracted text, or null if extraction failed or file type is unsupported.
     */
    fun extractTextFromFile(file: File): String? {
        if (file.name.endsWith(PDF_TEMP_SUFFIX)) {
            return extractTextFromPdf(file)
        }
        LOGGER.warning("Unsupported file type for text extraction: " + file.name)
        return null
    }

    /**
     * Saves the uploaded form value to a temporary file.
     *
     * @param formValue The form value representing the uploaded file.
     * @return The temporary file if successful, otherwise null.
     */
    private fun saveToTempFile(formValue: FormValue): File? {
        val uniqueFileName =
                String.format("%s_%s%s", PDF_TEMP_PREFIX, UUID.randomUUID(), PDF_TEMP_SUFFIX)
        try {
            val tempFile = File.createTempFile(uniqueFileName, PDF_TEMP_SUFFIX)
            tempFile.deleteOnExit()
            formValue.fileItem.inputStream.use { inputStream ->
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                return tempFile
            }
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "Failed to save uploaded PDF to temporary file.", e)
            return null
        }
    }

    /**
     * Extracts text content from a PDF file.
     *
     * @param pdfFile The PDF file from which to extract text.
     * @return The extracted text, or null if extraction failed.
     */
    private fun extractTextFromPdf(pdfFile: File): String? {
        try {
            Loader.loadPDF(pdfFile).use { document ->
                val stripper = PDFLayoutTextStripper()
                val text = stripper.getText(document)
                LOGGER.fine("Successfully extracted text from PDF.")
                return text
            }
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "Error extracting text from PDF: " + e.message)
            return null
        }
    }
}
