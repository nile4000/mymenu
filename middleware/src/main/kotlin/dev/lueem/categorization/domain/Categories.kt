package dev.lueem.categorization.domain

data class Category(
    var name: String = "",
    var icon: String = "",
    var color: String = "",
    var embeddingText: String = "",
    var fallback: Boolean = false,
)

data class CategoryConfig(
    var categories: List<Category> = emptyList(),
)
