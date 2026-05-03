package dev.lueem.extraction.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReceiptTextProcessorTest {

    private val processor = ReceiptTextProcessor()

    @Test
    fun normalizeReceiptText_removesEmptyLinesAndTrims() {
        val input = """

            Header

            Item 1

            Total

        """.trimIndent()

        val result = processor.normalizeReceiptText(input).replace("\r\n", "\n")

        assertEquals("Header\nItem 1\nTotal", result)
    }

    @Test
    fun extractRetailer_identifiesCoop() {
        val result = processor.extractRetailer("Welcome to Coop Supermarket")

        assertEquals("Coop", result)
    }

    @Test
    fun extractRetailer_identifiesMigros() {
        val result = processor.extractRetailer("Migros Genossenschaft")

        assertEquals("Migros", result)
    }

    @Test
    fun extractRetailer_returnsUnknownForUnrecognizedStore() {
        val result = processor.extractRetailer("Some other store")

        assertEquals("Unknown", result)
    }

    @Test
    fun extractTotal_findsValidAmount() {
        val receipt = """
            Item 1 2.50
            Total CHF 12.50
            Card Payment
        """.trimIndent()

        val result = processor.extractTotal(receipt)

        assertEquals("12.50", result)
    }

    @Test
    fun extractTotal_returnsDefaultOnMissing() {
        val result = processor.extractTotal("No total here")

        assertEquals("0.00", result)
    }

    @Test
    fun extractDate_findsSwissDate() {
        val receipt = "Date: 21.02.24 Time: 10:30"

        val result = processor.extractDate(receipt)

        assertEquals("21.02.24", result)
    }

    @Test
    fun extractDate_returnsIsoDateWhenNotFound() {
        val result = processor.extractDate("No date")

        assertTrue(result.matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }

    @Test
    fun extractArticlesSection_cutsAtTerminator() {
        val receipt = """
            Artikel Menge Preis Total
            Milk 1 1.50 1.50
            Bread 1 2.00 2.00
            Rabatt Bread -0.50
            Total CHF 3.50
            Points: 100
        """.trimIndent()

        val section = processor.extractArticlesSection(receipt)

        assertTrue(section.contains("Milk"))
        assertTrue(section.contains("Bread"))
        assertTrue(section.contains("Rabatt Bread"))
        assertTrue(!section.contains("Total CHF"))
    }

    @Test
    fun findTotalLineIndex_findsCorrectLine() {
        val receipt = """
            Header
            Artikel Menge Preis Total
            Milk 1 1.50 1.50
            Total CHF 1.50
        """.trimIndent()
        val normalized = processor.normalizeReceiptText(receipt)

        val result = processor.findTotalLineIndex(normalized)

        // 1-indexed: Header(1), header row(2), article(3), Total CHF(4)
        assertEquals(4, result)
    }

    @Test
    fun extractBarcode_findsLongNumericSequence() {
        val receipt = """
            Item 1
            1234567890123
            Total CHF 5.00
        """.trimIndent()

        val result = processor.extractBarcode(receipt)

        assertEquals("1234567890123", result)
    }

    @Test
    fun extractBarcode_returnsNullWhenNotPresent() {
        val result = processor.extractBarcode("No barcode here 123 456")

        assertNull(result)
    }
}
