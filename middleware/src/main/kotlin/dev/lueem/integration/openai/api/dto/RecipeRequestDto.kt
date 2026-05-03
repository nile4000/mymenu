package dev.lueem.integration.openai.api.dto

data class RecipeItem(
    var name: String = "",
    var quantity: Double = 0.0,
    var unit: String = "",
)

data class RecipeRequest(
    var items: List<RecipeItem> = emptyList(),
    var servings: Int = 1,
)
