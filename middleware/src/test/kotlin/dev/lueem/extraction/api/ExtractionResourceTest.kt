package dev.lueem.extraction.api

import dev.lueem.extraction.app.ExtractionService
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.io.File
import java.math.BigDecimal

@QuarkusTest
class ExtractionResourceTest {

    @InjectMock
    lateinit var extractionService: ExtractionService

    @Test
    fun extractPdf_returns415WithoutMultipartContentType() {
        given()
            .`when`().post("/api/extract-pdf")
            .then()
            .statusCode(415)
    }

    @Test
    fun extractPdf_returns422WhenNoPdfInRequest() {
        given()
            .contentType("multipart/form-data")
            .multiPart("file", "document.txt", "some text content".toByteArray())
            .`when`().post("/api/extract-pdf")
            .then()
            .statusCode(422)
            .body("code", equalTo("VALIDATION_ERROR"))
    }

    @Test
    fun extractPdf_returnsReceiptResponseForValidPdf() {
        val expected = ReceiptResponse(
            uid = "test-uid",
            purchaseDate = "24.02.21",
            corp = "Coop",
            total = BigDecimal("42.00"),
            articles = emptyList(),
            metadata = ReceiptMetadata(extractedTotalRow = 5, openAiArticleCount = 0)
        )
        `when`(extractionService.analyzeReceipt(anyArg())).thenReturn(expected)
        val pdf = File(javaClass.classLoader.getResource("kassenzettelpoc.pdf").toURI())

        given()
            .contentType("multipart/form-data")
            .multiPart("file", pdf, "application/pdf")
            .`when`().post("/api/extract-pdf")
            .then()
            .statusCode(200)
            .body("corp", equalTo("Coop"))
            .body("purchaseDate", equalTo("24.02.21"))
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> anyArg(): T = Mockito.any<T>() as T
}
