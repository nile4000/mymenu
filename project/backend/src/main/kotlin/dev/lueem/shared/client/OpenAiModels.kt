package dev.lueem.shared.client

data class OpenAiRequest(
    var model: String = "",
    var messages: List<OpenAiMessage> = emptyList(),
    var temperature: Double = 0.0
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
