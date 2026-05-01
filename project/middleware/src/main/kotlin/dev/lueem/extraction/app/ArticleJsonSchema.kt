package dev.lueem.extraction.app

data class ArticleFieldSpec(val name: String, val defaultValue: Any)

object ArticleJsonSchema {
    const val NAME_FIELD = "Name"
    const val PRICE_FIELD = "Price"
    const val QUANTITY_FIELD = "Quantity"
    const val DISCOUNT_FIELD = "Discount"
    const val TOTAL_FIELD = "Total"

    val FIELD_SPECS = listOf(
        ArticleFieldSpec(NAME_FIELD, ""),
        ArticleFieldSpec(PRICE_FIELD, 0.0),
        ArticleFieldSpec(QUANTITY_FIELD, 0.0),
        ArticleFieldSpec(DISCOUNT_FIELD, 0.0),
        ArticleFieldSpec(TOTAL_FIELD, 0.0)
    )
}
