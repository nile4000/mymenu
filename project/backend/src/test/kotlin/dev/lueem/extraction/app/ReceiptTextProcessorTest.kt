package dev.lueem.extraction.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReceiptTextProcessorTest {

    private val processor = ReceiptTextProcessor()

    @Test
    fun normalizeReceiptText_removesEmptyLinesAndTrims() {
        val input = """
            
            Header   
               
            Item 1
            
            Total
            
        """.trimIndent()
        val expected = "Header\nItem 1\nTotal"
        assertEquals(expected, processor.normalizeReceiptText(input).replace("\r\n", "\n"))
    }

    @Test
    fun extractRetailer_identifiesCoopAndMigros() {
        assertEquals("Coop", processor.extractRetailer("Welcome to Coop Supermarket"))
        assertEquals("Migros", processor.extractRetailer("Migros Genossenschaft"))
        assertThrows(IllegalArgumentException::class.java) {
            processor.extractRetailer("Some other store")
        }
    }

    @Test
    fun extractTotal_findsValidAmount() {
        val receipt = """
            Item 1 2.50
            Total CHF 12.50
            Card Payment
        """.trimIndent()
        assertEquals("12.50", processor.extractTotal(receipt))
    }

    @Test
    fun extractTotal_returnsDefaultOnMissing() {
        assertEquals("0.00", processor.extractTotal("No total here"))
    }

    @Test
    fun extractDate_findsSwissDate() {
        val receipt = "Date: 21.02.24 Time: 10:30"
        assertEquals("21.02.24", processor.extractDate(receipt))
    }

    @Test
    fun extractDate_returnsTimestampOnMissing() {
        val date = processor.extractDate("No date")
        // Check if it matches yyyyMMdd...
        assert(date.length >= 8)
    }

    @Test
    fun extractArticlesSection_cutsAtTerminator() {
        val receipt = """
            Artikel Menge Preis Total
            Milk 1 1.50 1.50
            Bread 1 2.00 2.00
            Total CHF 3.50
            Points: 100
        """.trimIndent()
        val section = processor.extractArticlesSection(receipt)
        assert(section.contains("Milk"))
        assert(section.contains("Bread"))
        assert(!section.contains("Total CHF"))
    }

    @Test
    fun findTotalLineIndex_findsCorrectLine() {
        val receipt = """
            Header
            Artikel Menge Preis Total
            Milk 1 1.50 1.50
            Total CHF 1.50
        """.trimIndent()
        // Line indices in normalizeReceiptText result (no empty lines)
        val normalized = processor.normalizeReceiptText(receipt)
        // 1: Header
        // 2: Artikel Menge Preis Total
        // 3: Milk 1 1.50 1.50
        // 4: Total CHF 1.50
        assertEquals(4, processor.findTotalLineIndex(normalized))
    }
}
