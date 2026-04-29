package dev.lueem.integration.supercard.infra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SupercardHtmlParserTest {

    private val parser = SupercardHtmlParser()

    @Test
    fun `parsePurchasesJson extracts location, date and amount`() {
        val json = """
            {
              "purchases": [
                {
                  "date": "2026-04-27T10:45:01+0000",
                  "total": {
                    "amount": 11640,
                    "currency": "CHF"
                  },
                  "location": {
                    "name": "Burgdorf Schuetzenmat",
                    "logo": [
                      {
                        "format": "LOGO_SINGLE_SVG",
                        "url": "https://contentimages.coop.ch/logos/logo_single_retail_de.svg"
                      }
                    ]
                  },
                  "encBarcode": "encoded-123",
                  "barcode": "9900050524627042600116402543"
                }
              ]
            }
        """.trimIndent()

        val links = parser.parsePurchasesJson(json)

        assertEquals(1, links.size)
        assertEquals("9900050524627042600116402543", links[0].externalReceiptId)
        assertEquals("Burgdorf Schuetzenmat", links[0].locationName)
        assertEquals("2026-04-27", links[0].purchaseDate)
        assertEquals("116.40", links[0].totalChf?.toPlainString())
        assertEquals("https://contentimages.coop.ch/logos/logo_single_retail_de.svg", links[0].logoUrl)
    }

    @Test
    fun `extractExternalReceiptId returns bc query parameter`() {
        val id = parser.extractExternalReceiptId("https://example.org/receipt?bc=abc123&pdfType=receipt")
        assertEquals("abc123", id)
    }

    @Test
    fun `extractExternalReceiptId falls back to original url when bc missing`() {
        val url = "https://example.org/receipt?id=1"
        val id = parser.extractExternalReceiptId(url)
        assertEquals(url, id)
        assertTrue(id.contains("receipt"))
    }
}
