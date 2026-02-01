package dev.lueem.extract

import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import java.io.IOException

class PDFLayoutTextStripper : PDFTextStripper() {

    companion object {
        const val DEBUG = false
        const val OUTPUT_SPACE_CHARACTER_WIDTH_IN_PT = 4
    }

    private var previousTextPosition: TextPosition? = null
    private val textLineList: MutableList<TextLine> = ArrayList()

}

class TextLine(lineLength: Int) {
    companion object {
        private const val SPACE_CHARACTER = ' '
    }

    private val lineLength: Int = lineLength / PDFLayoutTextStripper.OUTPUT_SPACE_CHARACTER_WIDTH_IN_PT
    var line: String = ""
        private set
    private var lastIndex: Int = 0

    init {
        completeLineWithSpaces()
    }

    fun writeCharacterAtIndex(character: LayoutCharacter) {
        character.index = computeIndexForCharacter(character)
        val index = character.index
        val characterValue = character.characterValue
        if (indexIsInBounds(index) && this.line[index] == SPACE_CHARACTER) {
            this.line = this.line.substring(0, index) + characterValue +
                    this.line.substring(index + 1, this.lineLength)
        }
    }

    fun getLineLength(): Int {
        return this.lineLength
    }

    private fun computeIndexForCharacter(character: LayoutCharacter): Int {
        var index = character.index
        val isCharacterPartOfPreviousWord = character.isCharacterPartOfPreviousWord
        val isCharacterAtTheBeginningOfNewLine = character.isCharacterAtTheBeginningOfNewLine
        val isCharacterCloseToPreviousWord = character.isCharacterCloseToPreviousWord

        if (!this.indexIsInBounds(index)) {
            return -1
        } else {
            if (isCharacterPartOfPreviousWord && !isCharacterAtTheBeginningOfNewLine) {
                index = this.findMinimumIndexWithSpaceCharacterFromIndex(index)
            } else if (isCharacterCloseToPreviousWord) {
                if (this.line[index] != SPACE_CHARACTER) {
                    index = index + 1
                } else {
                    index = this.findMinimumIndexWithSpaceCharacterFromIndex(index) + 1
                }
            }
            index = this.getNextValidIndex(index, isCharacterPartOfPreviousWord)
            return index
        }
    }

    private fun isSpaceCharacterAtIndex(index: Int): Boolean {
        return this.line[index] == SPACE_CHARACTER
    }

    private fun isNewIndexGreaterThanLastIndex(index: Int): Boolean {
        return index > this.lastIndex
    }

    private fun getNextValidIndex(index: Int, isCharacterPartOfPreviousWord: Boolean): Int {
        var nextValidIndex = index
        val lastIndex = this.lastIndex
        if (!this.isNewIndexGreaterThanLastIndex(index)) {
            nextValidIndex = lastIndex + 1
        }
        if (!isCharacterPartOfPreviousWord && this.isSpaceCharacterAtIndex(index - 1)) {
            nextValidIndex = nextValidIndex + 1
        }
        this.lastIndex = nextValidIndex
        return nextValidIndex
    }

    private fun findMinimumIndexWithSpaceCharacterFromIndex(index: Int): Int {
        var newIndex = index
        while (newIndex >= 0 && this.line[newIndex] == SPACE_CHARACTER) {
            newIndex = newIndex - 1
        }
        return newIndex + 1
    }

    private fun indexIsInBounds(index: Int): Boolean {
        return (index >= 0 && index < this.lineLength)
    }

    private fun completeLineWithSpaces() {
        for (i in 0 until this.lineLength) {
            line += SPACE_CHARACTER
        }
    }
}

class LayoutCharacter(
    val characterValue: Char,
    var index: Int,
    val isCharacterPartOfPreviousWord: Boolean,
    val isFirstCharacterOfAWord: Boolean,
    val isCharacterAtTheBeginningOfNewLine: Boolean,
    val isCharacterCloseToPreviousWord: Boolean
) {

    init {
        if (PDFLayoutTextStripper.DEBUG)
            println(this.toString())
    }

    override fun toString(): String {
        var toString = ""
        toString += index
        toString += " "
        toString += characterValue
        toString += " isCharacterPartOfPreviousWord=$isCharacterPartOfPreviousWord"
        toString += " isFirstCharacterOfAWord=$isFirstCharacterOfAWord"
        toString += " isCharacterAtTheBeginningOfNewLine=$isCharacterAtTheBeginningOfNewLine"
        toString += " isCharacterPartOfASentence=$isCharacterCloseToPreviousWord"
        toString += " isCharacterCloseToPreviousWord=$isCharacterCloseToPreviousWord"
        return toString
    }
}
