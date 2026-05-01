package dev.lueem.categorization.infra

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductNameCleanerTest {

    private val cleaner = ProductNameCleaner()

    @Test
    fun `removes multipack volume and promotion word`() {
        assertEquals(
            "Coca Cola Zero",
            cleaner.clean("2x Coca Cola Zero 1.5L Aktion")
        )
    }

    @Test
    fun `removes quantity and discount word`() {
        assertEquals(
            "Milch",
            cleaner.clean("Milch 500g Rabatt")
        )
    }

    @Test
    fun `removes leading price and trailing quantity`() {
        assertEquals(
            "Bananen",
            cleaner.clean("CHF 2.95 Bananen 1 kg")
        )
    }

    @Test
    fun `removes euro price`() {
        assertEquals(
            "Tomaten",
            cleaner.clean("\u20ac1.99 Tomaten")
        )
    }

    @Test
    fun `removes percentage promotion phrase`() {
        assertEquals(
            "Shampoo",
            cleaner.clean("20% auf Shampoo")
        )
    }

    @Test
    fun `falls back to original name when cleaned result is empty`() {
        assertEquals(
            "Fr. 3.50",
            cleaner.clean("Fr. 3.50")
        )
    }
}
