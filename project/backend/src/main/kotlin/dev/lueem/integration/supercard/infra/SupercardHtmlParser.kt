package dev.lueem.integration.supercard.infra

import jakarta.enterprise.context.ApplicationScoped
import jakarta.json.Json
import java.io.StringReader
import java.math.BigDecimal
import java.time.OffsetDateTime

@ApplicationScoped
class SupercardHtmlParser {

    fun parsePurchasesJson(json: String): List<SupercardReceiptLink> {
        val root = Json.createReader(StringReader(json)).readObject()
        val purchases = root.getJsonArray("purchases")
            ?: throw IllegalArgumentException("No 'purchases' array in JSON")

        return purchases.map { element ->
            val obj = element.asJsonObject()
            val enc = obj.getString("encBarcode")
            val barcode = obj.getString("barcode", enc)
            val url = "https://www.supercard.ch/bin/coop/kbk/kassenzettelpoc?bc=$enc&pdfType=receipt"
            val purchaseDate = obj.getString("date", null)
                ?.let { runCatching { OffsetDateTime.parse(it).toLocalDate().toString() }.getOrNull() }
            val totalChf = runCatching {
                val rappen = obj.getJsonObject("total").getJsonNumber("amount").longValue()
                BigDecimal(rappen).movePointLeft(2)
            }.getOrNull()
            SupercardReceiptLink(receiptUrl = url, externalReceiptId = barcode, purchaseDate = purchaseDate, totalChf = totalChf)
        }
    }

    fun extractExternalReceiptId(url: String): String {
        val bc = Regex("[?&]bc=([^&]+)").find(url)?.groupValues?.get(1)
        if (!bc.isNullOrBlank()) return bc
        return url
    }
}
