package dev.lueem.extraction.infra

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

/**
 * Keeps text extraction behavior stable after package refactor.
 */
class PDFLayoutTextStripper : PDFTextStripper() {
    init {
        sortByPosition = true
    }

    override fun getText(doc: PDDocument): String = super.getText(doc)
}
