package dev.lueem.integration.supercard.infra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SupercardHtmlParserTest {

    private val parser = SupercardHtmlParser()

    @Test
    fun `parseReceiptLinks extracts receipt urls and bc ids`() {
        val html = """
            <div class="receipt-button" data-receipturl="https://www.supercard.ch/bin/coop/kbk/kassenzettelpoc?bc=abc123&amp;pdfType=receipt"></div>
            <div class="receipt-button" data-receipturl="https://www.supercard.ch/bin/coop/kbk/kassenzettelpoc?bc=def456&amp;pdfType=receipt"></div>
        """.trimIndent()

        val links = parser.parseReceiptLinks(html)

        assertEquals(2, links.size)
        assertEquals("abc123", links[0].externalReceiptId)
        assertEquals("def456", links[1].externalReceiptId)
        assertTrue(links[0].receiptUrl.contains("pdfType=receipt"))
    }

    @Test
    fun `extractExternalReceiptId falls back to hash when bc missing`() {
        val id = parser.extractExternalReceiptId("https://example.org/receipt?id=1")
        assertEquals(64, id.length)
    }
}
