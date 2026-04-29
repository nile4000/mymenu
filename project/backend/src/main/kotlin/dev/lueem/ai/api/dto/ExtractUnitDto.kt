package dev.lueem.ai.api.dto

data class ExtractUnitItemDto(
    var id: String = "",
    var name: String = "",
    var quantity: Double = 0.0,
    var price: Double = 0.0,
)

data class ExtractUnitRequestDto(
    var items: List<ExtractUnitItemDto> = emptyList(),
)

data class ExtractUnitResultItemDto(
    var id: String = "",
    var unit: String = "",
)
