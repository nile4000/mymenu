package dev.lueem.model.openai


data class OpenAiRequest(
        val model: String,
        val messages: List<OpenAiMessage>,
        val temperature: Double
)

data class OpenAiMessage(
        var role: String = "",
        var content: String = ""
)

data class OpenAiResponse(
        var id: String? = null,
        var choices: List<OpenAiChoice>? = null
)

data class OpenAiChoice(
        var index: Int = 0,
        var message: OpenAiMessage? = null
)
