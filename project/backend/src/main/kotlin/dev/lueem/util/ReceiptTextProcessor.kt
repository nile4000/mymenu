package dev.lueem.util

import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import java.util.regex.Pattern

/**
 * Utility class for processing and extracting information from text content, typically used for
 * parsing receipts.
 */
@ApplicationScoped
class ReceiptTextProcessor {

    companion object {
        private val LOGGER = Logger.getLogger(ReceiptTextProcessor::class.java.name)

        // Regex patterns used by multiple steps.
        private val COOP_PATTERN = Pattern.compile("Coop", Pattern.CASE_INSENSITIVE)
        private val MIGROS_PATTERN = Pattern.compile("Migros", Pattern.CASE_INSENSITIVE)
        private val TOTAL_PATTERN =
                Pattern.compile("(?m)^\\bTotal CHF\\b.*", Pattern.CASE_INSENSITIVE)
        private val DATE_PATTERN = Pattern.compile("\\b\\d{2}\\.\\d{2}\\.\\d{2}\\b")
        private val HEADER_PATTERN =
                Pattern.compile(
                        "Artikel\\s+Menge\\s+Preis\\s+Aktion\\s+Total\\s+Zusatz",
                        Pattern.CASE_INSENSITIVE
                )
        private val TERMINATOR_PATTERN =
                Pattern.compile("(?m)^\\b(?:Total CHF|Rabatt|Bon)\\b.*", Pattern.CASE_INSENSITIVE)

        // Formatter for generating timestamps.
        private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    }

    /**
     * Cleans up the provided content by removing empty lines and trimming whitespace.
     *
     * @param content the raw text content from a receipt
     * @return a cleaned-up version of the text with no empty lines
     */
    fun normalizeReceiptText(content: String): String {
        return content.split(System.lineSeparator().toRegex())
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .joinToString(System.lineSeparator())
                .trim()
    }

    /**
     * Extracts the corporation name ("Coop" or "Migros") from the provided text.
     *
     * @param text the text to search for corporation names
     * @return the name of the corporation found in the text
     * @throws IllegalArgumentException if neither "Coop" nor "Migros" is found
     */
    fun extractRetailer(text: String): String {
        return if (COOP_PATTERN.matcher(text).find()) {
            "Coop"
        } else if (MIGROS_PATTERN.matcher(text).find()) {
            "Migros"
        } else {
            throw IllegalArgumentException("Neither Coop nor Migros found in text.")
        }
    }

    /**
     * Extracts the total amount from the receipt.
     *
     * @param receipt the receipt text containing the total
     * @return the total amount as a string (e.g., "123.45"), or "0.00" if not found
     */
    fun extractTotal(receipt: String): String {
        val matcher = TOTAL_PATTERN.matcher(receipt)
        if (matcher.find()) {
            val totalLine = matcher.group()
            val amountMatcher = Pattern.compile("\\d+\\.\\d{2}").matcher(totalLine)
            if (amountMatcher.find()) {
                return amountMatcher.group()
            }
        }
        return "0.00"
    }

    /**
     * Extracts the date from the text. If no date is found, returns the current timestamp.
     *
     * @param text the text to search for a date
     * @return the extracted date in the format "dd.MM.yy" or the current timestamp if not found
     */
    fun extractDate(text: String): String {
        val matcher = DATE_PATTERN.matcher(text)
        return if (matcher.find()) matcher.group() else currentTimestamp
    }

    /**
     * Extracts the articles section from the receipt, stopping before 'Total CHF', 'Rabatt', or
     * 'Bon'.
     *
     * @param receipt the full receipt text
     * @return the substring of the receipt containing the articles
     */
    fun extractArticlesSection(receipt: String): String {
        val terminatorMatcher = TERMINATOR_PATTERN.matcher(receipt)
        var terminatorIndex = -1

        if (terminatorMatcher.find()) {
            terminatorIndex = terminatorMatcher.start()
            val matchedLine = terminatorMatcher.group()
            LOGGER.fine("Terminating line found: \"$matchedLine\" at Index: $terminatorIndex")
        } else {
            LOGGER.fine("No terminating line found like bon, rabatt or total.")
        }

        if (terminatorIndex != -1) {
            return receipt.substring(0, terminatorIndex).trim()
        } else {
            LOGGER.fine("No Keyword found. Returning entire receipt.")
            return receipt
        }
    }

    /**
     * Finds the row number of the "Total CHF" row in the receipt. returns -1 if not found, else the
     * row number
     */
    fun findTotalLineIndex(receipt: String): Int {
        val lines = receipt.split(System.lineSeparator().toRegex()).toTypedArray()

        val firstArticleLine = findFirstArticleLineIndex(receipt)
        if (firstArticleLine == -1) {
            LOGGER.warning("First row of articles not found. Could not find 'Total'.")
            return -1
        }

        for (i in firstArticleLine until lines.size) {
            val matcher = TOTAL_PATTERN.matcher(lines[i])
            if (matcher.find()) {
                LOGGER.fine("\"Total CHF\" column found at line: " + (i + 1))
                return i + 1
            }
        }

        LOGGER.fine("\"Total CHF\" row not found.")
        return -1
    }

    private fun findFirstArticleLineIndex(receipt: String): Int {
        val lines = receipt.split(System.lineSeparator().toRegex()).toTypedArray()

        for (i in lines.indices) {
            val matcher = HEADER_PATTERN.matcher(lines[i])
            if (matcher.find()) {
                LOGGER.fine("First row found at line: " + (i + 1))
                return i + 1
            }
        }
        return -1
    }

    /**
     * Generates a current timestamp in the format "yyyyMMddHHmmss".
     *
     * @return the formatted current timestamp
     */
    private val currentTimestamp: String
        get() = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
}
