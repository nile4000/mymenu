package dev.lueem.category.domain

data class Category(
    val name: String,
    val icon: String,
    val color: String,
)

object Categories {
    val ALL: List<Category> = listOf(
        Category("Obst und Gemüse", "emoji_nature", "green-4"),
        Category("Milchprodukte, Eier und Alternativen", "local_dining", "blue-4"),
        Category("Fleisch, Fisch und pflanzliche Proteine", "restaurant_menu", "red-4"),
        Category("Backwaren und Getreide", "bakery_dining", "brown-4"),
        Category("Getränke (alkoholisch & alkoholfrei)", "local_cafe", "orange-4"),
        Category("Snacks, Apero und Süßwaren", "cake", "pink-4"),
        Category("Reinigungsmittel und Haushaltsreiniger", "soap", "grey-4"),
        Category("Körperpflegeprodukte und Hygieneartikel", "spa", "purple-4"),
        Category("Tierbedarf und Sonstiges", "pets", "teal-4"),
        Category("Tiefkühlprodukte", "ac_unit", "cyan-4"),
        Category("Konserven und Vorratsartikel", "inventory", "yellow-4"),
        Category("Gewürze, Kräuter und Saucen", "eco", "lime-4"),
    )

    val FALLBACK_NAME: String = "Andere"

    val NAMES: List<String> = ALL.map { it.name }
}
