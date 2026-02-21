package dev.lueem.shared.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

/**
 * Centralized OpenAI configuration. Single source of truth for all OpenAI-related
 * properties — eliminates duplication previously spread across OpenAiChatRequestFactory
 * and AiGatewayService.
 */
@ApplicationScoped
class OpenAiProperties {

    @ConfigProperty(name = "OPENAI_API_KEY")
    lateinit var apiKey: String

    @ConfigProperty(name = "OPENAI_MODEL", defaultValue = "gpt-4o-mini-2024-07-18")
    lateinit var defaultModel: String

    // Creativity of the model
    @ConfigProperty(name = "OPENAI_TEMPERATURE", defaultValue = "0.0")
    var temperature: Double = 0.0

    @ConfigProperty(name = "OPENAI_TEMPERATURE_RECIPE", defaultValue = "-1.0")
    var recipeTemperature: Double = -1.0

    @ConfigProperty(name = "OPENAI_MODEL_CATEGORIZATION", defaultValue = "")
    lateinit var categorizationModel: String

    @ConfigProperty(name = "OPENAI_MODEL_UNIT", defaultValue = "")
    lateinit var unitModel: String

    @ConfigProperty(name = "OPENAI_MODEL_RECIPE", defaultValue = "")
    lateinit var recipeModel: String

    fun resolveModel(override: String): String =
        if (override.isBlank()) defaultModel else override

    fun resolveTemperature(override: Double): Double =
        if (override < 0.0) temperature else override
}
