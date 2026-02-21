package dev.lueem.extraction.infra

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

/**
 * Infrastructure utility for handling PDF multipart uploads and text extraction.
 * Belongs in infra: depends on RESTEasy multipart types and PDFBox.
 */
object PdfFileHandler {
    private val LOGGER = Logger.getLogger(PdfFileHandler::class.java.name)
    private const val PDF_TEMP_PREFIX = "uploaded"
    private const val PDF_TEMP_SUFFIX = ".pdf"

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

    fun extractTextFromFile(file: File): String? {
        if (file.name.endsWith(PDF_TEMP_SUFFIX)) {
            return extractTextFromPdf(file)
        }
        LOGGER.warning("Unsupported file type for text extraction: ${file.name}")
        return null
    }

    private fun saveToTempFile(formValue: FormValue): File? {
        val uniqueFileName = "${PDF_TEMP_PREFIX}_${UUID.randomUUID()}$PDF_TEMP_SUFFIX"
        return try {
            val tempFile = File.createTempFile(uniqueFileName, PDF_TEMP_SUFFIX)
            tempFile.deleteOnExit()
            formValue.fileItem.inputStream.use { inputStream ->
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            tempFile
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "Failed to save uploaded PDF to temporary file.", e)
            null
        }
    }

    private fun extractTextFromPdf(pdfFile: File): String? {
        return try {
            Loader.loadPDF(pdfFile).use { document ->
                val stripper = PDFLayoutTextStripper()
                stripper.getText(document)
            }
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, "Error extracting text from PDF: ${e.message}")
            null
        }
    }
}
