package dev.lueem.categorization.infra

import dev.lueem.categorization.domain.Category
import dev.lueem.categorization.domain.CategoryConfig
import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.bind.JsonbBuilder

@ApplicationScoped
class CategoryCatalog {

    companion object {
        private const val CATEGORIES_RESOURCE = "/categories.json"
    }

    private val config: CategoryConfig = loadConfig()

    val all: List<Category> = config.categories
    val names: List<String> = all.map { it.name }
    val fallbackName: String? = all.firstOrNull { it.fallback }?.name

    private fun loadConfig(): CategoryConfig {
        val stream = requireNotNull(CategoryCatalog::class.java.getResourceAsStream(CATEGORIES_RESOURCE)) {
            "Missing category resource: $CATEGORIES_RESOURCE"
        }

        return stream.bufferedReader(Charsets.UTF_8).use { reader ->
            JsonbBuilder.create().use { jsonb ->
                jsonb.fromJson(reader, CategoryConfig::class.java)
            }
        }
    }
}
