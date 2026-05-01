package dev.lueem.categorization.api

import dev.lueem.categorization.infra.CategorizationClient
import dev.lueem.categorization.api.dto.Categorize
import dev.lueem.categorization.api.dto.CategorizeRequest
import dev.lueem.categorization.api.dto.CategorizeResult
import dev.lueem.shared.error.UploadCategorizationException
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`

@QuarkusTest
class CategoryResourceTest {

    @InjectMock
    lateinit var categorizationClient: CategorizationClient

    @Test
    fun categorizeRejectsEmptyItems() {
        given()
            .contentType("application/json")
            .body("""{"items":[]}""")
            .`when`().post("/api/categories/categorize")
            .then()
            .statusCode(422)
            .body("code", equalTo("VALIDATION_ERROR"))
            .body("message", containsString("leer"))
    }

    @Test
    fun categorizeReturnsTypedResponse() {
        val request = CategorizeRequest(items = listOf(Categorize(id = "1", name = "Item")))
        val response = listOf(CategorizeResult(id = "1", category = "Andere"))
        `when`(categorizationClient.categorize(request)).thenReturn(response)

        given()
            .contentType("application/json")
            .body("""{"items":[{"id":"1","name":"Item"}]}""")
            .`when`().post("/api/categories/categorize")
            .then()
            .statusCode(200)
            .body("[0].id", equalTo("1"))
            .body("[0].category", equalTo("Andere"))
    }

    @Test
    fun categorizeReturnsBadGatewayOnUploadError() {
        val request = CategorizeRequest(items = listOf(Categorize(id = "1", name = "Item")))
        `when`(categorizationClient.categorize(request))
            .thenThrow(UploadCategorizationException("Categorization service HTTP 429"))

        given()
            .contentType("application/json")
            .body("""{"items":[{"id":"1","name":"Item"}]}""")
            .`when`().post("/api/categories/categorize")
            .then()
            .statusCode(502)
    }
}
