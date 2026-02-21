package dev.lueem.extraction.api

import dev.lueem.extraction.app.ExtractionService
import dev.lueem.extraction.infra.PdfFileHandler
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput

@Path("/api")
class ExtractionResource @Inject constructor(private val extractionService: ExtractionService) {

    /** Accepts a multipart upload and extracts receipt data from a PDF file. */
    @POST
    @Path("extract")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun extractArticlesFromPdf(input: MultipartFormDataInput): Response {
        val pdfFile = PdfFileHandler.extractPdfFromMultipart(input)
            ?: throw IllegalArgumentException("No valid PDF file found in the request.")

        val receiptResponse = extractionService.analyzeReceipt(pdfFile)
        return Response.ok(receiptResponse).build()
    }
}
