package dev.lueem.categorization.infra

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ProductNameCleaner {

    companion object {
        private val PRICE_REGEX = Regex(
            pattern = """(?i)(?:\b(?:CHF|Fr\.?|EUR)\s*\d+(?:[.,]\d{1,2})?\b|\u20ac\s*\d+(?:[.,]\d{1,2})?\b|\b\d+(?:[.,]\d{1,2})?\s*(?:CHF|Fr\.?|EUR|\u20ac)\b)"""
        )
        private val MULTIPACK_REGEX = Regex("""(?i)\b\d+\s*x\b""")
        private val QUANTITY_REGEX = Regex("""(?i)\b\d+(?:[.,]\d+)?\s*(?:kg|g|l|ml)\b""")
        private val PERCENT_REGEX = Regex("""[-+]?\d+(?:[.,]\d+)?\s*%""")
        private val PROMO_WORD_REGEX = Regex("""(?i)\b(?:aktion|rabatt|sale|angebot|reduziert|statt|nur|auf)\b""")
        private val WHITESPACE_REGEX = Regex("""\s+""")
        private val REMOVABLE_PART_REGEXES = listOf(
            PRICE_REGEX,
            MULTIPACK_REGEX,
            QUANTITY_REGEX,
            PERCENT_REGEX,
            PROMO_WORD_REGEX
        )
    }

    fun clean(name: String): String {
        val normalizedOriginal = normalizeWhitespace(name)
        if (normalizedOriginal.isBlank()) return normalizedOriginal

        val cleaned = REMOVABLE_PART_REGEXES
            .fold(normalizedOriginal) { current, regex -> regex.replace(current, " ") }
            .let(::trimSeparators)
            .let(::normalizeWhitespace)

        return cleaned.ifBlank { normalizedOriginal }
    }

    private fun normalizeWhitespace(value: String): String =
        value.replace(WHITESPACE_REGEX, " ").trim()

    private fun trimSeparators(value: String): String =
        value.trim().trim('-', ',', '.', ':', ';')
}
