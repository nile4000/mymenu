package dev.lueem.service

import dev.lueem.client.OpenAiClient
import dev.lueem.model.ReceiptMetadata
import dev.lueem.model.ReceiptResponse
import dev.lueem.util.PdfFileHandler
import dev.lueem.util.ReceiptTextProcessor
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.json.Json
import jakarta.json.JsonArray
import java.io.File
import java.math.BigDecimal
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

private val LOGGER = Logger.getLogger(ExtractionService::class.java.name)
private const val QUESTION_PREFIX =
        "Extract articles from the given receipt and return a list in a valid JSON format.\n" +
                "Each article should include the following fields: Name, Price, Quantity, Discount, Total (or 0 if none).\n"

@ApplicationScoped
class ExtractionService
@Inject
constructor(
        private val openAiClient: OpenAiClient,
        private val textProcessor: ReceiptTextProcessor,
        private val articleMapper: ArticleJsonMapper
) {

    /** Orchestrates the full receipt extraction pipeline. */
    fun analyzeReceipt(pdfFile: File): ReceiptResponse {
        try {
            val documentContent =
                    PdfFileHandler.extractTextFromFile(pdfFile)
                            ?: throw IllegalArgumentException(
                                    "Failed to extract text from PDF file."
                            )

            val cleanedContent = textProcessor.normalizeReceiptText(documentContent)
            val extractTotal = textProcessor.extractTotal(cleanedContent)
            val extractDate = textProcessor.extractDate(cleanedContent)
            val totalRowNumber = textProcessor.findTotalLineIndex(cleanedContent)
            val articlesSection = textProcessor.extractArticlesSection(cleanedContent)

            val articlesJson = requestOpenAiArticles(articlesSection)
            if (articlesJson.isEmpty()) {
                LOGGER.severe("Failed to retrieve articles from OpenAI.")
                throw RuntimeException("Failed to retrieve articles from OpenAI.")
            }

            val sanitizedArticlesJson = articleMapper.sanitizeArticleJsonArray(articlesJson)

            val articles = articleMapper.mapToArticles(sanitizedArticlesJson)

            val corp =
                    try {
                        textProcessor.extractRetailer(cleanedContent)
                    } catch (e: IllegalArgumentException) {
                        "Unknown"
                    }

            val uid = UUID.randomUUID().toString()
            val totalAmount = BigDecimal(extractTotal)

            return ReceiptResponse(
                    uid = uid,
                    purchaseDate = extractDate,
                    corp = corp,
                    total = totalAmount,
                    articles = articles,
                    metadata =
                            ReceiptMetadata(
                                    extractedTotalRow =
                                            if (totalRowNumber >= 0) totalRowNumber else 0,
                                    openAiArticleCount =
                                            if (articles.isNotEmpty()) articles.size else 0
                            )
            )
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "Error in extracting Articles from PDF", e)
            throw e
        }
    }

    private fun requestOpenAiArticles(extractedText: String): JsonArray {
        val fullQuestion = QUESTION_PREFIX + extractedText
        return try {
            val response = openAiClient.extractArticlesFromText(fullQuestion)
            if (response.isEmpty()) {
                Json.createArrayBuilder().build()
            } else response
        } catch (e: Exception) {
            LOGGER.log(Level.SEVERE, "Error while communicating with OpenAI", e)
            Json.createArrayBuilder().build()
        }
    }
}
