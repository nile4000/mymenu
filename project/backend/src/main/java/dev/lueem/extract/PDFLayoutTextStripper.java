
package dev.lueem.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PDFLayoutTextStripper extends PDFTextStripper {

    public static final boolean DEBUG = false;
    public static final int OUTPUT_SPACE_CHARACTER_WIDTH_IN_PT = 4;

    private double currentPageWidth;
    private TextPosition previousTextPosition;
    private List<TextLine> textLineList;

    public PDFLayoutTextStripper() throws IOException {
        super();
        this.previousTextPosition = null;
        this.textLineList = new ArrayList<TextLine>();
    }

}

class TextLine {

    private static final char SPACE_CHARACTER = ' ';
    private final int lineLength;
    private String line;
    private int lastIndex;

    public TextLine(int lineLength) {
        this.line = "";
        this.lineLength = lineLength / PDFLayoutTextStripper.OUTPUT_SPACE_CHARACTER_WIDTH_IN_PT;
        this.completeLineWithSpaces();
    }

    public void writeCharacterAtIndex(final Character character) {
        character.setIndex(this.computeIndexForCharacter(character));
        int index = character.getIndex();
        char characterValue = character.getCharacterValue();
        if (this.indexIsInBounds(index) && this.line.charAt(index) == SPACE_CHARACTER) {
            this.line = this.line.substring(0, index) + characterValue
                    + this.line.substring(index + 1, this.getLineLength());
        }
    }

    public int getLineLength() {
        return this.lineLength;
    }

    public String getLine() {
        return line;
    }

    private int computeIndexForCharacter(final Character character) {
        int index = character.getIndex();
        boolean isCharacterPartOfPreviousWord = character.isCharacterPartOfPreviousWord();
        boolean isCharacterAtTheBeginningOfNewLine = character.isCharacterAtTheBeginningOfNewLine();
        boolean isCharacterCloseToPreviousWord = character.isCharacterCloseToPreviousWord();

        if (!this.indexIsInBounds(index)) {
            return -1;
        } else {
            if (isCharacterPartOfPreviousWord && !isCharacterAtTheBeginningOfNewLine) {
                index = this.findMinimumIndexWithSpaceCharacterFromIndex(index);
            } else if (isCharacterCloseToPreviousWord) {
                if (this.line.charAt(index) != SPACE_CHARACTER) {
                    index = index + 1;
                } else {
                    index = this.findMinimumIndexWithSpaceCharacterFromIndex(index) + 1;
                }
            }
            index = this.getNextValidIndex(index, isCharacterPartOfPreviousWord);
            return index;
        }
    }

    /**
     * Checks if the character at the given index in the line is a space.
     *
     * @param index position within the line
     * @return {@code true} if the character is a space, otherwise {@code false}
     */
    private boolean isSpaceCharacterAtIndex(int index) {
        return this.line.charAt(index) == SPACE_CHARACTER;
    }

    private boolean isNewIndexGreaterThanLastIndex(int index) {
        int lastIndex = this.getLastIndex();
        return (index > lastIndex);
    }

    private int getNextValidIndex(int index, boolean isCharacterPartOfPreviousWord) {
        int nextValidIndex = index;
        int lastIndex = this.getLastIndex();
        if (!this.isNewIndexGreaterThanLastIndex(index)) {
            nextValidIndex = lastIndex + 1;
        }
        if (!isCharacterPartOfPreviousWord && this.isSpaceCharacterAtIndex(index - 1)) {
            nextValidIndex = nextValidIndex + 1;
        }
        this.setLastIndex(nextValidIndex);
        return nextValidIndex;
    }

    private int findMinimumIndexWithSpaceCharacterFromIndex(int index) {
        int newIndex = index;
        while (newIndex >= 0 && this.line.charAt(newIndex) == SPACE_CHARACTER) {
            newIndex = newIndex - 1;
        }
        return newIndex + 1;
    }

    private boolean indexIsInBounds(int index) {
        return (index >= 0 && index < this.lineLength);
    }

    private void completeLineWithSpaces() {
        for (int i = 0; i < this.getLineLength(); ++i) {
            line += SPACE_CHARACTER;
        }
    }

    private int getLastIndex() {
        return this.lastIndex;
    }

    private void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

}

class Character {

    private final char characterValue;
    private int index;
    private final boolean isCharacterPartOfPreviousWord;
    private final boolean isFirstCharacterOfAWord;
    private final boolean isCharacterAtTheBeginningOfNewLine;
    private final boolean isCharacterCloseToPreviousWord;

    public Character(char characterValue, int index, boolean isCharacterPartOfPreviousWord,
            boolean isFirstCharacterOfAWord, boolean isCharacterAtTheBeginningOfNewLine,
            boolean isCharacterPartOfASentence) {
        this.characterValue = characterValue;
        this.index = index;
        this.isCharacterPartOfPreviousWord = isCharacterPartOfPreviousWord;
        this.isFirstCharacterOfAWord = isFirstCharacterOfAWord;
        this.isCharacterAtTheBeginningOfNewLine = isCharacterAtTheBeginningOfNewLine;
        this.isCharacterCloseToPreviousWord = isCharacterPartOfASentence;
        if (PDFLayoutTextStripper.DEBUG)
            System.out.println(this.toString());
    }

    public char getCharacterValue() {
        return this.characterValue;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isCharacterPartOfPreviousWord() {
        return this.isCharacterPartOfPreviousWord;
    }

    public boolean isFirstCharacterOfAWord() {
        return this.isFirstCharacterOfAWord;
    }

    public boolean isCharacterAtTheBeginningOfNewLine() {
        return this.isCharacterAtTheBeginningOfNewLine;
    }

    public boolean isCharacterCloseToPreviousWord() {
        return this.isCharacterCloseToPreviousWord;
    }

    @Override
    public String toString() {
        String toString = "";
        toString += index;
        toString += " ";
        toString += characterValue;
        toString += " isCharacterPartOfPreviousWord=" + isCharacterPartOfPreviousWord;
        toString += " isFirstCharacterOfAWord=" + isFirstCharacterOfAWord;
        toString += " isCharacterAtTheBeginningOfNewLine=" + isCharacterAtTheBeginningOfNewLine;
        toString += " isCharacterPartOfASentence=" + isCharacterCloseToPreviousWord;
        toString += " isCharacterCloseToPreviousWord=" + isCharacterCloseToPreviousWord;
        return toString;
    }

}
