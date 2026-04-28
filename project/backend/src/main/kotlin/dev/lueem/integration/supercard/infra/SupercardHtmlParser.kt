package dev.lueem.integration.supercard.infra

import jakarta.enterprise.context.ApplicationScoped
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@ApplicationScoped
class SupercardHtmlParser {

    private val receiptUrlAttrRegex = Regex("""data-receipturl\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val directReceiptUrlRegex = Regex(
        """https://www\.supercard\.ch/bin/coop/kbk/kassenzettelpoc\?[^"'\s<]+""",
        RegexOption.IGNORE_CASE
    )

    fun parseReceiptLinks(html: String): List<SupercardReceiptLink> {
        val attrLinks = receiptUrlAttrRegex.findAll(html).map { it.groupValues[1] }
        val directLinks = directReceiptUrlRegex.findAll(html).map { it.value }

        val links = attrLinks
            .plus(directLinks)
            .map { decodeHtmlAttribute(it) }
            .map { normalizeReceiptUrl(it) }
            .distinct()
            .toList()

        if (links.isEmpty()) {
            throw IllegalArgumentException("No receipt links found in Supercard response")
        }

        return links.map { link ->
            SupercardReceiptLink(
                receiptUrl = link,
                externalReceiptId = extractExternalReceiptId(link)
            )
        }
    }

    fun extractExternalReceiptId(url: String): String {
        val bc = Regex("[?&]bc=([^&]+)").find(url)?.groupValues?.get(1)
        if (!bc.isNullOrBlank()) {
            return bc
        }
        val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun decodeHtmlAttribute(value: String): String =
        value.replace("&amp;", "&")

    private fun normalizeReceiptUrl(raw: String): String {
        val decoded = URLDecoder.decode(raw, StandardCharsets.UTF_8)
        return if (decoded.startsWith("http")) decoded else "https://www.supercard.ch$decoded"
    }
}
