package dev.lueem.shared.util

/** Strips markdown code fences and extracts the first JSON payload of the expected type. */
object JsonSanitizer {

    fun sanitize(content: String, expectedStart: Char, anchorKey: String? = null): String {
        val trimmed = stripCodeFence(content.trim())
        return extractFirstJsonPayload(trimmed, expectedStart, anchorKey) ?: trimmed
    }

    private fun stripCodeFence(content: String): String {
        var trimmed = content
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```")
            val firstNewline = trimmed.indexOf('\n')
            if (firstNewline >= 0) {
                trimmed = trimmed.substring(firstNewline + 1)
            }
            trimmed = trimmed.removeSuffix("```").trim()
        }
        return trimmed
    }

    private fun extractFirstJsonPayload(
        text: String,
        expectedStart: Char,
        anchorKey: String?
    ): String? {
        val expectedEnd = when (expectedStart) {
            '[' -> ']'
            '{' -> '}'
            else -> return null
        }
        val start = findStart(text, expectedStart, anchorKey)
        if (start < 0) return null

        var depth = 0
        var inString = false
        var escape = false

        for (i in start until text.length) {
            val ch = text[i]
            if (inString) {
                when {
                    escape -> escape = false
                    ch == '\\' -> escape = true
                    ch == '"' -> inString = false
                }
                continue
            }

            if (ch == '"') {
                inString = true
                continue
            }

            when (ch) {
                expectedStart -> depth++
                expectedEnd -> {
                    depth--
                    if (depth == 0) return text.substring(start, i + 1)
                }
            }
        }

        return null
    }

    private fun findStart(text: String, expectedStart: Char, anchorKey: String?): Int {
        if (anchorKey.isNullOrBlank()) return text.indexOf(expectedStart)
        val anchorIndex = text.indexOf(anchorKey)
        if (anchorIndex < 0) return text.indexOf(expectedStart)
        return text.lastIndexOf(expectedStart, anchorIndex)
    }
}
