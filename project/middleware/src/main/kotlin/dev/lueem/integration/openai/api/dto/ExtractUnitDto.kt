package dev.lueem.integration.openai.api.dto

data class ExtractUnitItem(
    var id: String = "",
    var name: String = "",
    var quantity: Double = 0.0,
    var price: Double = 0.0,
)

data class ExtractUnitRequest(
    var items: List<ExtractUnitItem> = emptyList(),
)

data class ExtractUnitResultItem(
    var id: String = "",
    var unit: String = "",
)
