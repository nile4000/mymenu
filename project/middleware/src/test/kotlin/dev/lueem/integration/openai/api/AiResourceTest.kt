package dev.lueem.integration.openai.api

import dev.lueem.integration.openai.app.AiGatewayService
import dev.lueem.integration.openai.api.dto.ExtractUnitItem
import dev.lueem.integration.openai.api.dto.ExtractUnitRequest
import dev.lueem.integration.openai.api.dto.RecipeItem
import dev.lueem.integration.openai.api.dto.RecipeRequest
import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`

@QuarkusTest
class AiResourceTest {

    @InjectMock
    lateinit var aiGatewayService: AiGatewayService

    @Test
    fun extractUnitRejectsTooManyItems() {
        val tooManyRequest = ExtractUnitRequest(
            items = (1..41).map { ExtractUnitItem(id = "$it", name = "Item $it", quantity = 1.0, price = 1.0) }
        )
        `when`(aiGatewayService.extractUnit(tooManyRequest))
            .thenThrow(IllegalArgumentException("items must not contain more than 40 entries"))

        val tooManyItemsJson = (1..41).joinToString(",") {
            """{"id":"$it","name":"Item $it","quantity":1,"price":1}"""
        }

        given()
            .contentType("application/json")
            .body("""{"items":[$tooManyItemsJson]}""")
            .`when`().post("/api/ai/extract-unit")
            .then()
            .statusCode(422)
            .body("code", equalTo("VALIDATION_ERROR"))
            .body("message", containsString("40"))
    }

    @Test
    fun recipeRejectsInvalidServings() {
        val request = RecipeRequest(
            items = listOf(RecipeItem(name = "Reis", quantity = 1.0, unit = "kg")),
            servings = 11
        )
        `when`(aiGatewayService.recipe(request))
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

}
