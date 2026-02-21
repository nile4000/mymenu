package dev.lueem.ai.api

import dev.lueem.ai.app.AiGatewayService
import dev.lueem.shared.error.UpstreamOpenAiException
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`

@QuarkusTest
class AiResourceTest {

    @InjectMock
    lateinit var aiGatewayService: AiGatewayService

    @Test
    fun categorizeRejectsEmptyItems() {
        `when`(aiGatewayService.categorize(any(CategorizeRequest::class.java)))
            .thenThrow(IllegalArgumentException("items must not be empty"))

        given()
            .contentType("application/json")
            .body("""{"items":[]}""")
            .`when`().post("/api/ai/categorize")
            .then()
            .statusCode(422)
            .body("code", equalTo("VALIDATION_ERROR"))
            .body("message", containsString("items"))
    }

    @Test
    fun extractUnitRejectsTooManyItems() {
        `when`(aiGatewayService.extractUnit(any(ExtractUnitRequest::class.java)))
            .thenThrow(IllegalArgumentException("items must not contain more than 40 entries"))

        val tooManyItems = (1..41).joinToString(",") {
            """{"id":"$it","name":"Item $it","quantity":1,"price":1}"""
        }

        given()
            .contentType("application/json")
            .body("""{"items":[$tooManyItems]}""")
            .`when`().post("/api/ai/extract-unit")
            .then()
            .statusCode(422)
            .body("code", equalTo("VALIDATION_ERROR"))
            .body("message", containsString("40"))
    }

    @Test
    fun recipeRejectsInvalidServings() {
        `when`(aiGatewayService.recipe(any(RecipeRequest::class.java)))
            .thenThrow(IllegalArgumentException("servings must be between 1 and 10"))

        given()
            .contentType("application/json")
            .body("""{"items":[{"name":"Reis","quantity":1,"unit":"kg"}],"servings":11}""")
            .`when`().post("/api/ai/recipe")
            .then()
            .statusCode(422)
            .body("code", equalTo("VALIDATION_ERROR"))
            .body("message", containsString("servings"))
    }

    @Test
    fun categorizeReturnsTypedResponse() {
        val response = listOf(CategorizeResultItem(id = "1", category = "Andere"))
        `when`(aiGatewayService.categorize(any(CategorizeRequest::class.java))).thenReturn(response)

        given()
            .contentType("application/json")
            .body("""{"items":[{"id":"1","name":"Item"}]}""")
            .`when`().post("/api/ai/categorize")
            .then()
            .statusCode(200)
            .body("[0].id", equalTo("1"))
            .body("[0].category", equalTo("Andere"))
    }

    @Test
    fun categorizeReturnsBadGatewayOnUpstreamError() {
        `when`(aiGatewayService.categorize(any(CategorizeRequest::class.java)))
            .thenThrow(UpstreamOpenAiException("OpenAI HTTP 429"))

        given()
            .contentType("application/json")
            .body("""{"items":[{"id":"1","name":"Item"}]}""")
            .`when`().post("/api/ai/categorize")
            .then()
            .statusCode(502)
            .body("code", equalTo("UPSTREAM_ERROR"))
            .body("message", equalTo("Upstream AI call failed"))
    }
}
