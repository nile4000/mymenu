import { Ref, ref } from "vue";

export const formatArticlesForCategorization = (articles: any[]): string => {
  return articles
    .map((article) => `ID: ${article.id}, Name: ${article.name}`)
    .join("\n");
};

export const categorySystemPrompt: Ref<string> = ref(
  "You are a categorization-assistant. Provide only valid JSON strictly in the format [{id: string, category: string}] without any additional text or formatting."
);
export const categorizationPrompt = (batch: any[]) => {
  const formattedArticles = formatArticlesForCategorization(batch);
  const categoriesList = categories.map((category) => `${category}`).join("\n");
  return `Categorize the following articles based on their names:\n
          ${formattedArticles}
          Use the following categories for classification:\n
          ${categoriesList}
          **Rules:**
          - If an article does not fit into any of these categories, assign it the category 'Andere'.
          - Use strictly the following JSON format: [{id: string, category: string}].`;
};

export function validateExtractedCategories(
  categorizedArticles: {
    id: string;
    category: string;
  }[]
) {
  const validCategorizedArticles = categorizedArticles.filter(
    (article) =>
      article.id &&
      typeof article.id === "string" &&
      article.category &&
      typeof article.category === "string" &&
      article.category.trim() !== ""
  );

  return validCategorizedArticles;
}

export const categories: string[] = [
  "Obst und Gemüse",
  "Milchprodukte, Eier und Alternativen",
  "Fleisch, Fisch und pflanzliche Proteine",
  "Backwaren und Getreide",
  "Getränke (alkoholisch & alkoholfrei)",
  "Snacks, Apero und Süßwaren",
  "Reinigungsmittel und Haushaltsreiniger",
  "Körperpflegeprodukte und Hygieneartikel",
  "Tierbedarf und Sonstiges",
  "Tiefkühlprodukte",
  "Konserven und Vorratsartikel",
  "Gewürze, Kräuter und Saucen",
];

// src/components/prompts/categorization.ts
export const categoryIcon: { name: string; icon: string; color: string }[] = [
  { name: "Obst und Gemüse", icon: "emoji_nature", color: "green-4" },
  { name: "Milchprodukte, Eier und Alternativen", icon: "local_dining", color: "blue-4" },
  { name: "Fleisch, Fisch und pflanzliche Proteine", icon: "restaurant_menu", color: "red-4" },
  { name: "Backwaren und Getreide", icon: "bakery_dining", color: "brown-4" },
  { name: "Getränke (alkoholisch & alkoholfrei)", icon: "local_cafe", color: "orange-4" },
  { name: "Snacks, Apero und Süßwaren", icon: "cake", color: "pink-4" },
  { name: "Reinigungsmittel und Haushaltsreiniger", icon: "soap", color: "grey-4" },
  { name: "Körperpflegeprodukte und Hygieneartikel", icon: "spa", color: "purple-4" },
  { name: "Tierbedarf und Sonstiges", icon: "pets", color: "teal-4" },
  { name: "Tiefkühlprodukte", icon: "ac_unit", color: "cyan-4" },
  { name: "Konserven und Vorratsartikel", icon: "inventory", color: "yellow-4" },
  { name: "Gewürze, Kräuter und Saucen", icon: "eco", color: "lime-4" },
];

