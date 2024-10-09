package dev.lueem.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {


    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("discount")
    private BigDecimal discount;

    @JsonProperty("total")
    private BigDecimal total;

    @JsonProperty("category")
    private String category;

    // optional?
    @JsonProperty("id")
    private Long id;

}
