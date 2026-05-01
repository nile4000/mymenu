package dev.lueem.integration.openai.domain

data class RecipeIngredient(
    var name: String = "",
    var amount: Double = 0.0,
    var unit: String = "",
)

data class Recipe(
    var title: String = "",
    var description: String = "",
    var cookingTime: String = "",
    var category: String = "",
    var servings: Int = 1,
    var color: String = "",
    var ingredients: List<RecipeIngredient> = emptyList(),
    var stepsList: List<String> = emptyList(),
    var image: String = "",
)
