package dev.lueem.category.api.dto

data class CategorizeItem(
    var id: String = "",
    var name: String = "",
)

data class CategorizeRequest(
    var items: List<CategorizeItem> = emptyList(),
)

data class CategorizeResultItem(
    var id: String = "",
    var category: String = "",
)
