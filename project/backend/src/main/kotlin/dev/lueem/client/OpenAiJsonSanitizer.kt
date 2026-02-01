package dev.lueem.client

object OpenAiJsonSanitizer {

    fun sanitize(content: String): String {
        var trimmed = content.trim()
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```")
            val firstNewline = trimmed.indexOf('\n')
            if (firstNewline >= 0) {
                trimmed = trimmed.substring(firstNewline + 1)
            }
            trimmed = trimmed.removeSuffix("```").trim()
        }
        return extractFirstJsonObject(trimmed, "\"ArticleList\"") ?: trimmed
    }

    private fun extractFirstJsonObject(text: String, anchorKey: String): String? {
        val anchorIndex = text.indexOf(anchorKey)
        val start =
            if (anchorIndex >= 0) {
                text.lastIndexOf('{', anchorIndex)
            } else {
                text.indexOf('{')
            }
        if (start < 0) return null

        var depth = 0
        var inString = false
        var escape = false

        for (i in start until text.length) {
            val ch = text[i]
            if (inString) {
                if (escape) {
                    escape = false
                } else if (ch == '\\') {
                    escape = true
                } else if (ch == '"') {
                    inString = false
                }
                continue
            } else if (ch == '"') {
                inString = true
                continue
            }

            if (ch == '{') {
                depth++
            } else if (ch == '}') {
                depth--
                if (depth == 0) {
                    return text.substring(start, i + 1)
                }
            }
        }

        return null
    }
}
