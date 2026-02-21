package dev.lueem.ai.api

class CategorizeItem {
    var id: String = ""
    var name: String = ""
}

class ExtractUnitItem {
    var id: String = ""
    var name: String = ""
    var quantity: Double = 0.0
    var price: Double = 0.0
}

class RecipeItem {
    var name: String = ""
    var quantity: Double = 0.0
    var unit: String = ""
}

class CategorizeRequest {
    var items: List<CategorizeItem> = emptyList()
}

class ExtractUnitRequest {
    var items: List<ExtractUnitItem> = emptyList()
}

class RecipeRequest {
    var items: List<RecipeItem> = emptyList()
    var servings: Int = 1
}

data class CategorizeResultItem(
    var id: String = "",
    var category: String = ""
)

data class ExtractUnitResultItem(
    var id: String = "",
    var unit: String = ""
)

data class RecipeIngredientResponse(
    var name: String = "",
    var amount: Double = 0.0,
    var unit: String = ""
)

data class RecipeResponse(
    var title: String = "",
    var description: String = "",
    var cookingTime: String = "",
    var category: String = "",
    var servings: Int = 1,
    var color: String = "",
    var ingredients: List<RecipeIngredientResponse> = emptyList(),
    var stepsList: List<String> = emptyList(),
    var image: String = ""
)
