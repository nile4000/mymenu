package dev.lueem.ai.api.dto

data class CategorizeItemDto(
    var id: String = "",
    var name: String = "",
)

data class CategorizeRequestDto(
    var items: List<CategorizeItemDto> = emptyList(),
)

data class CategorizeResultItemDto(
    var id: String = "",
    var category: String = "",
)
