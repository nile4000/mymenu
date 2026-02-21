package dev.lueem.ai.app

import dev.lueem.ai.api.CategorizeRequest
import dev.lueem.ai.api.CategorizeResultItem
import dev.lueem.ai.api.ExtractUnitRequest
import dev.lueem.ai.api.ExtractUnitResultItem
import dev.lueem.ai.api.RecipeRequest
import dev.lueem.ai.api.RecipeResponse
import dev.lueem.shared.client.OpenAiClient
import dev.lueem.shared.config.OpenAiProperties
import dev.lueem.shared.error.UpstreamOpenAiException
import dev.lueem.shared.util.JsonSanitizer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.bind.JsonbBuilder

@ApplicationScoped
class AiGatewayService @Inject constructor(
    private val openAiClient: OpenAiClient,
    private val properties: OpenAiProperties
) {
    companion object {
        private val CATEGORIES = listOf(
            "Obst und Gemuese",
            "Milchprodukte, Eier und Alternativen",
            "Fleisch, Fisch und pflanzliche Proteine",
            "Backwaren und Getreide",
            "Getraenke (alkoholisch & alkoholfrei)",
            "Snacks, Apero und Suesswaren",
            "Reinigungsmittel und Haushaltsreiniger",
            "Koerperpflegeprodukte und Hygieneartikel",
            "Tierbedarf und Sonstiges",
            "Tiefkuehlprodukte",
            "Konserven und Vorratsartikel",
            "Gewuerze, Kraeuter und Saucen"
        )

        private const val CATEGORIZATION_SYSTEM_PROMPT =
            "You are a categorization assistant. Return only valid JSON in the exact format " +
                "[{\"id\":\"string\",\"category\":\"string\"}] with no markdown and no extra text."

        private const val UNIT_SYSTEM_PROMPT =
            "You are a text extraction assistant. Return only valid JSON in the exact format " +
                "[{\"id\":\"string\",\"unit\":\"string\"}] with no markdown and no extra text."

        private const val RECIPE_SCHEMA_DESCRIPTION =
            "{ title: string, description: string, cookingTime: string, category: string, " +
                "servings: number, color: string, ingredients: [{ name: string, amount: number, " +
                "unit: string }], stepsList: string[], image: string }"

        private val RECIPE_SYSTEM_PROMPT =
            "You are a cooking-recipe assistant. You will receive a list of ingredients and " +
            "should return a single JSON object in this structure: $RECIPE_SCHEMA_DESCRIPTION. " +
            "Return only one valid JSON object. No markdown, no comments, no extra text. " +
            "Use only edible ingredients from the user list and ignore non-food items. " +
            "\"servings\" must be numeric and match the requested servings exactly. " +
            "Each ingredient must include name (string), amount (number), and unit (non-empty string). " +
            "stepsList must be a non-empty array of strings. " +
            "Output language is strictly German (Deutsch) for title, description, category and every stepsList entry. " +
            "Any English wording is invalid. " +
            "If uncertain, still return your best valid JSON object."

        private val NON_FOOD_HINTS = listOf(
            "spul", "spül", "reiniger", "haushalt", "wasch", "seife", "shampoo", "hygiene", "tier", "cat", "dog"
        )
    }

    private val jsonb = JsonbBuilder.create()

    fun categorize(request: CategorizeRequest): List<CategorizeResultItem> {
        validateCategorizeRequest(request)
        val itemsText = request.items.joinToString("\n") { "ID: ${it.id}, Name: ${it.name}" }
        val categories = CATEGORIES.joinToString("\n")
        val userPrompt =
            "Categorize these articles:\n$itemsText\n" +
                "Use only these categories:\n$categories\n" +
                "If none fits, use category \"Andere\"."

        val content = openAiClient.chatCompletion(
            CATEGORIZATION_SYSTEM_PROMPT,
            userPrompt,
            properties.resolveModel(properties.categorizationModel),
            properties.temperature
        )
        return parseCategorizeResponse(content)
    }

    fun extractUnit(request: ExtractUnitRequest): List<ExtractUnitResultItem> {
        validateExtractUnitRequest(request)
        val itemsText = request.items.joinToString("\n") {
            "ID: ${it.id}, Name: ${it.name}, Quantity: ${it.quantity}, Price: ${it.price}"
        }
        val userPrompt =
            "Extract unit for these articles:\n$itemsText\n" +
                "Return unit values in lowercase and keep multiplicators. " +
                "Allowed units: g, kg, stk, l, ml, cl. " +
                "If no unit exists in the name, choose a sensible default (kg or stk)."

        val content = openAiClient.chatCompletion(
            UNIT_SYSTEM_PROMPT,
            userPrompt,
            properties.resolveModel(properties.unitModel),
            properties.temperature
        )
        return parseExtractUnitResponse(content)
    }

    fun recipe(request: RecipeRequest): RecipeResponse {
        validateRecipeRequest(request)
        val filteredItems = request.items.filter {
            it.quantity > 0.0 &&
                it.name.isNotBlank() &&
                it.unit.isNotBlank() &&
                NON_FOOD_HINTS.none { hint -> it.name.lowercase().contains(hint) }
        }
        require(filteredItems.isNotEmpty()) {
            "recipe requires at least one edible ingredient with quantity > 0"
        }

        val ingredientsList = filteredItems.mapIndexed { index, item ->
            "${index + 1}. ${item.name}, Menge: ${item.quantity}, Einheit: ${item.unit}"
        }.joinToString("\n")
        val userPrompt =
            "Hier sind meine verfuegbaren Zutaten:\n$ingredientsList\n" +
                "Erstelle ein gutes Rezept fuer ca. ${request.servings} Personen, " +
                "verwende nur Zutaten aus der Liste und keine weiteren."

        val content = openAiClient.chatCompletion(
            RECIPE_SYSTEM_PROMPT,
            userPrompt,
            properties.resolveModel(properties.recipeModel),
            properties.resolveTemperature(properties.recipeTemperature)
        )
        return parseRecipeResponse(content)
    }

    private fun validateCategorizeRequest(request: CategorizeRequest) {
        require(request.items.isNotEmpty()) { "items must not be empty" }
        require(request.items.size <= 40) { "items must not contain more than 40 entries" }
        require(request.items.all { it.id.isNotBlank() && it.name.isNotBlank() }) {
            "each item requires id and name"
        }
    }

    private fun validateExtractUnitRequest(request: ExtractUnitRequest) {
        require(request.items.isNotEmpty()) { "items must not be empty" }
        require(request.items.size <= 40) { "items must not contain more than 40 entries" }
        require(request.items.all {
            it.id.isNotBlank() && it.name.isNotBlank() && it.quantity.isFinite() && it.price.isFinite()
        }) { "each item requires id, name, quantity and price" }
    }

    private fun validateRecipeRequest(request: RecipeRequest) {
        require(request.items.isNotEmpty()) { "items must not be empty" }
        require(request.items.size <= 30) { "items must not contain more than 30 entries" }
        require(request.servings in 1..10) { "servings must be between 1 and 10" }
        require(request.items.all { it.name.isNotBlank() && it.unit.isNotBlank() && it.quantity.isFinite() }) {
            "each item requires name, quantity and unit"
        }
    }

    private fun parseCategorizeResponse(content: String): List<CategorizeResultItem> {
        val sanitized = JsonSanitizer.sanitize(content, '[')
        val parsed = runCatching {
            jsonb.fromJson(sanitized, Array<CategorizeResultItem>::class.java).toList()
        }.getOrElse {
            throw UpstreamOpenAiException("Invalid categorize JSON from AI", it)
        }
        require(parsed.isNotEmpty()) { "AI categorize response must not be empty" }
        require(parsed.all { it.id.isNotBlank() && it.category.isNotBlank() }) {
            "AI categorize response contains invalid entries"
        }
        return parsed
    }

    private fun parseExtractUnitResponse(content: String): List<ExtractUnitResultItem> {
        val sanitized = JsonSanitizer.sanitize(content, '[')
        val parsed = runCatching {
            jsonb.fromJson(sanitized, Array<ExtractUnitResultItem>::class.java).toList()
        }.getOrElse {
            throw UpstreamOpenAiException("Invalid extract-unit JSON from AI", it)
        }
        require(parsed.isNotEmpty()) { "AI extract-unit response must not be empty" }
        require(parsed.all { it.id.isNotBlank() }) {
            "AI extract-unit response contains invalid ids"
        }
        return parsed
    }

    private fun parseRecipeResponse(content: String): RecipeResponse {
        val sanitized = JsonSanitizer.sanitize(content, '{')
        val parsed = runCatching {
            jsonb.fromJson(sanitized, RecipeResponse::class.java)
        }.getOrElse {
            throw UpstreamOpenAiException("Invalid recipe JSON from AI", it)
        }
        require(parsed.title.isNotBlank()) { "AI recipe response must contain title" }
        require(parsed.ingredients.isNotEmpty()) { "AI recipe response must contain ingredients" }
        require(parsed.ingredients.all {
            it.name.isNotBlank() && it.amount.isFinite() && it.unit.isNotBlank()
        }) { "AI recipe response contains invalid ingredients" }
        require(parsed.stepsList.isNotEmpty() && parsed.stepsList.all { it.isNotBlank() }) {
            "AI recipe response must contain non-empty stepsList"
        }
        return parsed
    }
}
