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
