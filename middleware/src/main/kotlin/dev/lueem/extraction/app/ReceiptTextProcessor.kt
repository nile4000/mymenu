package dev.lueem.extraction.app

import jakarta.enterprise.context.ApplicationScoped
import java.util.logging.Logger

/**
 * Pure text-processing logic for parsing supermarket receipts from Coop or Migros.
 */
@ApplicationScoped
class ReceiptTextProcessor {

    companion object {
        private val LOGGER = Logger.getLogger(ReceiptTextProcessor::class.java.name)

        private val COOP_REGEX = Regex("Coop", RegexOption.IGNORE_CASE)
        private val MIGROS_REGEX = Regex("Migros", RegexOption.IGNORE_CASE)
        private val TOTAL_REGEX = Regex("""(?m)^\bTotal CHF\b.*""", RegexOption.IGNORE_CASE)
        private val TOTAL_AMOUNT_REGEX = Regex("""\d+\.\d{2}""")
        private val DATE_REGEX = Regex("""\b\d{2}\.\d{2}\.(\d{4}|\d{2})\b""")
        private val HEADER_REGEX = Regex(
            """Artikel\s+Menge\s+Preis(?:\s+Aktion)?\s+Total(?:\s+Zusatz)?""",
            RegexOption.IGNORE_CASE
        )
        private val TERMINATOR_REGEX = Regex("""(?m)^\b(?:Total CHF|Bon)\b.*""", RegexOption.IGNORE_CASE)

        // Kassenbon-Barcode: mindestens 10 aufeinanderfolgende Ziffern, allein auf einer Zeile
        private val BARCODE_REGEX = Regex("""(?m)^\s*(\d{10,})\s*$""")
    }

    fun normalizeReceiptText(content: String): String =
        content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(System.lineSeparator())
            .trim()

    fun extractRetailer(text: String): String = when {
        COOP_REGEX.containsMatchIn(text) -> "Coop"
        MIGROS_REGEX.containsMatchIn(text) -> "Migros"
        else -> "Unknown"
    }

    fun extractTotal(receipt: String): String {
        val totalLine = TOTAL_REGEX.find(receipt)?.value ?: return "0.00"
        return TOTAL_AMOUNT_REGEX.find(totalLine)?.value ?: "0.00"
    }

    fun extractDate(text: String): String {
        return DATE_REGEX.find(text)?.value ?: currentTimestamp
    }

    fun extractArticlesSection(receipt: String): String {
        val lines = receipt.lines()

        val headerIdx = lines.indexOfFirst { HEADER_REGEX.containsMatchIn(it) }
        val articleLines = if (headerIdx >= 0) {
            LOGGER.fine("Header row at line ${headerIdx + 1}; article section starts at line ${headerIdx + 2}")
            lines.drop(headerIdx + 1)
        } else {
            LOGGER.warning("Header row not found; article section starts from beginning.")
            lines
        }

        val terminatorIdx = articleLines.indexOfFirst { TERMINATOR_REGEX.containsMatchIn(it) }
        val sectionLines = if (terminatorIdx >= 0) {
            LOGGER.fine("Terminating line at article-section line ${terminatorIdx + 1}: \"${articleLines[terminatorIdx]}\"")
            articleLines.take(terminatorIdx)
        } else {
            LOGGER.fine("No terminator line found; using end of receipt.")
            articleLines
        }

        return sectionLines.joinToString(System.lineSeparator()).trim()
    }

    fun extractBarcode(text: String): String? {
        return BARCODE_REGEX.find(text)?.groupValues?.get(1)
    }

    fun findTotalLineIndex(receipt: String): Int {
        val firstArticleLine = findFirstArticleLineIndex(receipt)
        if (firstArticleLine == -1) {
            LOGGER.warning("First article row not found; cannot find Total.")
            return -1
        }
        val lines = receipt.lines()
        for (i in firstArticleLine until lines.size) {
            if (TOTAL_REGEX.containsMatchIn(lines[i])) {
                LOGGER.fine("\"Total CHF\" found at line ${i + 1}")
                return i + 1
            }
        }
        LOGGER.fine("\"Total CHF\" row not found.")
        return -1
    }

    private fun findFirstArticleLineIndex(receipt: String): Int {
        val lines = receipt.lines()
        for (i in lines.indices) {
            if (HEADER_REGEX.containsMatchIn(lines[i])) {
                LOGGER.fine("Header row at line ${i + 1}")
                return i + 1
            }
        }
        return -1
    }

    // ISO format so parseDate() in the repository can handle it without a special case
    private val currentTimestamp: String
        get() = java.time.LocalDate.now().toString()
}
