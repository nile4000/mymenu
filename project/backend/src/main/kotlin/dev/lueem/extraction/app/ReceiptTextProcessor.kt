package dev.lueem.extraction.app

import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Pure text-processing logic for parsing Swiss supermarket receipts.
 * No external dependencies — belongs in the app layer, easily unit-testable.
 */
@ApplicationScoped
class ReceiptTextProcessor {

    companion object {
        private val LOGGER = Logger.getLogger(ReceiptTextProcessor::class.java.name)

        private val COOP_PATTERN = Pattern.compile("Coop", Pattern.CASE_INSENSITIVE)
        private val MIGROS_PATTERN = Pattern.compile("Migros", Pattern.CASE_INSENSITIVE)
        private val TOTAL_PATTERN = Pattern.compile("(?m)^\\bTotal CHF\\b.*", Pattern.CASE_INSENSITIVE)
        private val DATE_PATTERN = Pattern.compile("\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b")
        private val HEADER_PATTERN = Pattern.compile(
            "Artikel\\s+Menge\\s+Preis\\s+Aktion\\s+Total\\s+Zusatz",
            Pattern.CASE_INSENSITIVE
        )
        private val TERMINATOR_PATTERN = Pattern.compile(
            "(?m)^\\b(?:Total CHF|Rabatt|Bon)\\b.*", Pattern.CASE_INSENSITIVE
        )
        private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }

    fun normalizeReceiptText(content: String): String =
        content.split(System.lineSeparator())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(System.lineSeparator())
            .trim()

    fun extractRetailer(text: String): String = when {
        COOP_PATTERN.matcher(text).find() -> "Coop"
        MIGROS_PATTERN.matcher(text).find() -> "Migros"
        else -> throw IllegalArgumentException("Neither Coop nor Migros found in text.")
    }

    fun extractTotal(receipt: String): String {
        val matcher = TOTAL_PATTERN.matcher(receipt)
        if (matcher.find()) {
            val amountMatcher = Pattern.compile("\\d+\\.\\d{2}").matcher(matcher.group())
            if (amountMatcher.find()) return amountMatcher.group()
        }
        return "0.00"
    }

    fun extractDate(text: String): String {
        val matcher = DATE_PATTERN.matcher(text)
        return if (matcher.find()) matcher.group() else currentTimestamp
    }

    fun extractArticlesSection(receipt: String): String {
        val matcher = TERMINATOR_PATTERN.matcher(receipt)
        return if (matcher.find()) {
            val idx = matcher.start()
            LOGGER.fine("Terminating line at index $idx: \"${matcher.group()}\"")
            receipt.substring(0, idx).trim()
        } else {
            LOGGER.fine("No terminator line found; returning entire receipt.")
            receipt
        }
    }

    fun findTotalLineIndex(receipt: String): Int {
        val firstArticleLine = findFirstArticleLineIndex(receipt)
        if (firstArticleLine == -1) {
            LOGGER.warning("First article row not found; cannot find Total.")
            return -1
        }
        val lines = receipt.split(System.lineSeparator())
        for (i in firstArticleLine until lines.size) {
            if (TOTAL_PATTERN.matcher(lines[i]).find()) {
                LOGGER.fine("\"Total CHF\" found at line ${i + 1}")
                return i + 1
            }
        }
        LOGGER.fine("\"Total CHF\" row not found.")
        return -1
    }

    private fun findFirstArticleLineIndex(receipt: String): Int {
        val lines = receipt.split(System.lineSeparator())
        for (i in lines.indices) {
            if (HEADER_PATTERN.matcher(lines[i]).find()) {
                LOGGER.fine("Header row at line ${i + 1}")
                return i + 1
            }
        }
        return -1
    }

    private val currentTimestamp: String
        get() = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
}
