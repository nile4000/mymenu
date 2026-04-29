package dev.lueem.ai.api.dto

data class RecipeItemDto(
    var name: String = "",
    var quantity: Double = 0.0,
    var unit: String = "",
)

data class RecipeRequestDto(
    var items: List<RecipeItemDto> = emptyList(),
    var servings: Int = 1,
)
