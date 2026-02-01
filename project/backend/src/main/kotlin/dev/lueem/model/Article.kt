package dev.lueem.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class Article(
    @JsonProperty("name")
    var name: String? = null,

    @JsonProperty("price")
    var price: BigDecimal? = null,

    @JsonProperty("quantity")
    var quantity: BigDecimal? = null,

    @JsonProperty("discount")
    var discount: BigDecimal? = null,

    @JsonProperty("total")
    var total: BigDecimal? = null,

    @JsonProperty("category")
    var category: String? = null,

    @JsonProperty("id")
    var id: Long? = null
)
