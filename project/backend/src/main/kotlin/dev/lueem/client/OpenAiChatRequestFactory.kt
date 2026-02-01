package dev.lueem.client

import dev.lueem.model.openai.OpenAiMessage
import dev.lueem.model.openai.OpenAiRequest
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

/**
 * Creates the OpenAI chat completion request payload.
 */
@ApplicationScoped
class OpenAiChatRequestFactory {

    @ConfigProperty(name = "OPENAI_MODEL", defaultValue = "gpt-3.5-turbo-0125")
    lateinit var modelDefault: String

    @ConfigProperty(name = "OPENAI_TEMPERATURE", defaultValue = "0.0")
    var temperature: Double = 0.0

    companion object {
        private const val SYSTEM_PROMPT_TEMPLATE =
            "You are an assistant that provides information in JSON format. " +
                "Please adhere strictly to the following structure:\n"
        // Example payload for the system prompt to show the expected JSON shape.
        private const val EXAMPLE_JSON_STRUCTURE = """
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
    }

    fun buildChatCompletionRequest(question: String): OpenAiRequest {
        val systemContent = buildSystemPrompt()
        return OpenAiRequest(
            model = modelDefault,
            messages =
                listOf(
                    OpenAiMessage("system", systemContent),
                    OpenAiMessage("user", question)
                ),
            temperature = temperature
        )
    }

    private fun buildSystemPrompt(): String = SYSTEM_PROMPT_TEMPLATE + EXAMPLE_JSON_STRUCTURE.trimIndent()
}
