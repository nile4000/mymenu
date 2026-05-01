package dev.lueem.ai.app

import dev.lueem.ai.api.dto.ExtractUnitRequest
import dev.lueem.ai.api.dto.ExtractUnitResultItem
import dev.lueem.ai.api.dto.RecipeRequest
import dev.lueem.ai.domain.Recipe
import dev.lueem.category.api.dto.CategorizeRequest
import dev.lueem.category.api.dto.CategorizeResultItem
import dev.lueem.category.infra.CategorizationClient
import dev.lueem.shared.client.OpenAiClient
import dev.lueem.shared.config.OpenAiProperties
import dev.lueem.shared.error.UploadOpenAiException
import dev.lueem.shared.util.JsonSanitizer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.bind.JsonbBuilder

@ApplicationScoped
class AiGatewayService @Inject constructor(
    private val openAiClient: OpenAiClient,
    private val categorizationClient: CategorizationClient,
    private val properties: OpenAiProperties
) {
    companion object {
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

    // Categorization now runs through our internal AI service instead of direct OpenAI prompts.
    fun categorize(request: CategorizeRequest): List<CategorizeResultItem> {
        validateCategorizeRequest(request)
        return categorizationClient.categorize(request)
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

    fun recipe(request: RecipeRequest): Recipe {
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
        require(request.items.isNotEmpty()) { "Die Liste darf nicht leer sein" }
        require(request.items.size <= 40) { "Die Liste darf maximal 40 Einträge enthalten" }
        require(request.items.all { it.id.isNotBlank() && it.name.isNotBlank() }) {
            "Jeder Eintrag benötigt eine ID und einen Namen"
        }
    }

    private fun validateExtractUnitRequest(request: ExtractUnitRequest) {
        require(request.items.isNotEmpty()) { "Die Liste darf nicht leer sein" }
        require(request.items.size <= 40) { "Die Liste darf maximal 40 Einträge enthalten" }
        require(request.items.all {
            it.id.isNotBlank() && it.name.isNotBlank() && it.quantity.isFinite() && it.price.isFinite()
        }) { "Jeder Eintrag benötigt eine ID, einen Namen, eine Menge und einen Preis" }
    }

    private fun validateRecipeRequest(request: RecipeRequest) {
        require(request.items.isNotEmpty()) { "Die Liste darf nicht leer sein" }
        require(request.items.size <= 30) { "Die Liste darf maximal 30 Einträge enthalten" }
        require(request.servings in 1..10) { "Die Portionsanzahl muss zwischen 1 und 10 liegen" }
        require(request.items.all { it.name.isNotBlank() && it.unit.isNotBlank() && it.quantity.isFinite() }) {
            "Jeder Eintrag benötigt einen Namen, eine Menge und eine Einheit"
        }
    }

    private fun parseExtractUnitResponse(content: String): List<ExtractUnitResultItem> {
        val sanitized = JsonSanitizer.sanitize(content, '[')
        val parsed = runCatching {
            jsonb.fromJson(sanitized, Array<ExtractUnitResultItem>::class.java).toList()
        }.getOrElse {
            throw UploadOpenAiException("Invalid extract-unit JSON from AI", it)
        }
        require(parsed.isNotEmpty()) { "AI extract-unit response must not be empty" }
        require(parsed.all { it.id.isNotBlank() }) {
            "AI extract-unit response contains invalid ids"
        }
        return parsed
    }

    private fun parseRecipeResponse(content: String): Recipe {
        val sanitized = JsonSanitizer.sanitize(content, '{')
        val parsed = runCatching {
            jsonb.fromJson(sanitized, Recipe::class.java)
        }.getOrElse {
            throw UploadOpenAiException("Invalid recipe JSON from AI", it)
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
