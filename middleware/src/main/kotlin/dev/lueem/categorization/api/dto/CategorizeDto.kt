package dev.lueem.categorization.api.dto

data class Categorize(
    var id: String = "",
    var name: String = "",
)

data class CategorizeRequest(
    var items: List<Categorize> = emptyList(),
)

data class CategorizeResult(
    var id: String = "",
    var category: String = "",
)
