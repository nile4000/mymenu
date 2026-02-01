package dev.lueem

import dev.lueem.service.ExtractionService
import dev.lueem.util.PdfFileHandler
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
        if (pdfFile == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No valid PDF file found in the request.")
                    .build()
        }

        return try {
            val receiptResponse = extractionService.analyzeReceipt(pdfFile)
            Response.ok(receiptResponse).build()
        } catch (e: Exception) {
            Response.serverError().entity(e.message).build()
        }
    }
}
