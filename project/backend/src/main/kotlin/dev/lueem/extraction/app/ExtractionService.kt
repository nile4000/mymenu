package dev.lueem.extraction.app

import dev.lueem.extraction.api.ReceiptMetadata
import dev.lueem.extraction.api.ReceiptResponse
import dev.lueem.extraction.infra.PdfFileHandler
import dev.lueem.shared.client.OpenAiClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.Json
import jakarta.json.JsonArray
import jakarta.json.JsonValue
import java.io.File
import java.math.BigDecimal
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

private val LOGGER = Logger.getLogger(ExtractionService::class.java.name)

@ApplicationScoped
class ExtractionService @Inject constructor(
    private val openAiClient: OpenAiClient,
    private val textProcessor: ReceiptTextProcessor,
    private val articleMapper: ArticleMapper,
    private val articleListParser: dev.lueem.extraction.infra.OpenAiArticleListParser,
    private val properties: dev.lueem.shared.config.OpenAiProperties
) {
    companion object {
        private const val EXTRACTION_SYSTEM_PROMPT =
            "You are an assistant that provides information in JSON format. " +
                "Please adhere strictly to the following structure:\n"
        private const val EXTRACTION_EXAMPLE_JSON = """
            {
              "Name": "QP Fruechtequark Erdbeer 2x125g",
              "ArticleList": [
                {
                  "Price": 1.2,
                  "Quantity": 1.0,
                  "Discount": 0.0,
                  "Total": 1.2
                }
              ]
            }
        """
        private const val QUESTION_PREFIX =
            "Extract articles from the given receipt and return a list in a valid JSON format.\n" +
                "Each article should include the following fields: Name, Price, Quantity, Discount, Total (or 0 if none).\n"
    }

    private val jsonb = jakarta.json.bind.JsonbBuilder.create()

    /** Orchestrates the full receipt extraction pipeline. */
    fun analyzeReceipt(pdfFile: File): ReceiptResponse {
        val traceId = UUID.randomUUID().toString().substring(0, 8)
        LOGGER.info("[extract:$traceId] Start extraction for file='${pdfFile.name}' size=${pdfFile.length()} bytes")

        val documentContent = PdfFileHandler.extractTextFromFile(pdfFile)
            ?: throw IllegalArgumentException("Failed to extract text from PDF file.")

        val cleanedContent = textProcessor.normalizeReceiptText(documentContent)
        LOGGER.info(
            "[extract:$traceId] OCR normalized content chars=${cleanedContent.length} lines=${countLines(cleanedContent)}"
        )
        val extractTotal = textProcessor.extractTotal(cleanedContent)
        val extractDate = textProcessor.extractDate(cleanedContent)
        val totalRowNumber = textProcessor.findTotalLineIndex(cleanedContent)
        val articlesSection = textProcessor.extractArticlesSection(cleanedContent)
        LOGGER.info(
            "[extract:$traceId] Article section chars=${articlesSection.length} lines=${countLines(articlesSection)}"
        )
        if (articlesSection.length < 40) {
            LOGGER.warning("[extract:$traceId] Article section is unusually short; OCR/section split likely degraded.")
        }

        val articlesJson = requestOpenAiArticles(traceId, articlesSection)
        if (articlesJson.isEmpty()) {
            LOGGER.severe("[extract:$traceId] Failed to retrieve articles from OpenAI.")
            throw RuntimeException("Failed to retrieve articles from OpenAI.")
        }
        val (rawMissingNames, rawBlankNames) = countNameIssues(articlesJson)
        LOGGER.info(
            "[extract:$traceId] OpenAI article list size=${articlesJson.size} missingName=$rawMissingNames blankName=$rawBlankNames"
        )

        val sanitizedJson = articleMapper.sanitizeArticleJsonArray(articlesJson, traceId)
        val domainArticles = articleMapper.mapToArticles(sanitizedJson, traceId)
        val blankMappedNames = domainArticles.count { it.name.isBlank() || it.name == "Unknown" }
        LOGGER.info(
            "[extract:$traceId] Mapped domain articles=${domainArticles.size} blankOrUnknownNames=$blankMappedNames"
        )
        val articleDtos = domainArticles.map { it.toDto() }

        val corp = try {
            textProcessor.extractRetailer(cleanedContent)
        } catch (e: IllegalArgumentException) {
            "Unknown"
        }
        LOGGER.info(
            "[extract:$traceId] Final response corp='$corp' purchaseDate='$extractDate' total=$extractTotal items=${articleDtos.size}"
        )

        return ReceiptResponse(
            uid = UUID.randomUUID().toString(),
            purchaseDate = extractDate,
            corp = corp,
            total = BigDecimal(extractTotal),
            articles = articleDtos,
            metadata = ReceiptMetadata(
                extractedTotalRow = if (totalRowNumber >= 0) totalRowNumber else 0,
                openAiArticleCount = articleDtos.size
            )
        )
    }

    private fun requestOpenAiArticles(traceId: String, extractedText: String): JsonArray {
        return try {
            val systemContent = EXTRACTION_SYSTEM_PROMPT + EXTRACTION_EXAMPLE_JSON.trimIndent()
            val userPrompt = QUESTION_PREFIX + extractedText
            LOGGER.info(
                "[extract:$traceId] Prompt prepared model='${properties.defaultModel}' systemChars=${systemContent.length} userChars=${userPrompt.length}"
            )

            val content = openAiClient.chatCompletion(
                systemContent,
                userPrompt,
                properties.defaultModel,
                properties.temperature
            )
            LOGGER.info("[extract:$traceId] OpenAI raw content chars=${content.length}")
            LOGGER.fine("[extract:$traceId] OpenAI raw content preview='${preview(content)}'")
            articleListParser.parseArticleListFromContent(content)
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "[extract:$traceId] Error while communicating with OpenAI", e)
            Json.createArrayBuilder().build()
        }
    }

    private fun countNameIssues(articlesJson: JsonArray): Pair<Int, Int> {
        var missing = 0
        var blank = 0
        articlesJson.forEach { value ->
            if (value.valueType != JsonValue.ValueType.OBJECT) {
                return@forEach
            }
            val obj = value.asJsonObject()
            if (!obj.containsKey("Name") || obj.isNull("Name")) {
                missing++
                return@forEach
            }
            runCatching { obj.getString("Name") }
                .onSuccess { if (it.isBlank()) blank++ }
                .onFailure { missing++ }
        }
        return missing to blank
    }

    private fun countLines(text: String): Int =
        if (text.isBlank()) 0 else text.lineSequence().count()

    private fun preview(text: String, maxLen: Int = 320): String =
        text.replace("\n", "\\n").replace("\r", "\\r").take(maxLen)
}
