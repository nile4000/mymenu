import { ref, Ref } from "vue";

export const formatArticlesForDetailExtraction = (articles: any[]): string => {
  return articles
    .map((article) => {
      if (article.quantity < 1) {
        return `Id: ${article.id}, Name: ${article.name} x ${article.quantity}kg`;
      } else if (article.quantity === 1) {
        // Handling for quantity equal to 1
        return `Id: ${article.id}, Name: ${article.name}, Quantity: ${article.quantity}`;
      } else {
        // Handling for quantities greater than 1
        return `Id: ${article.id}, Name: ${article.name} x ${article.quantity}`;
      }
    })
    .join("\n");
};

export const validateExtractedDetails = (
  extractedDetails: {
    id: string;
    unit: string;
  }[]
) => {
  const validExtractedDetails = extractedDetails.filter(
    (detail) =>
      detail.id &&
      typeof detail.id === "string" &&
      (typeof detail.unit === "string" || detail.unit === "")
  );
  return validExtractedDetails;
};

export const detailSystemPrompt: Ref<string> = ref(
  "You are an text-extraction assistant. Provide only valid JSON strictlyin the format [{id: string, unit: string}] without any additional text or formatting."
);
export const detailExtractionPrompt = (batch: any[]) => {
  const formattedArticles = formatArticlesForDetailExtraction(batch);
  return `Extract unit and the quantity for the following articles based on their Name:/n
          ${formattedArticles}
  For each article, provide the following information in JSON format:
  - **id**: The article's Id as a string, enclosed in quotes.
  - **unit**: The unit and quantity extracted from **Name** in lowercase letters. Examples are: "100g","200ml","2stk","33cl","1kg","10x10ml",8x60g,"2st ca. 330g, 10St 53g+").

  **Rules:**
  - **Valid units are only: "g","kg","stk","l","ml","cl".**
  - **Always copy the multiplicators.**
  - **If no unit is found in article Name. default to appropriate "kg" or "stk".**
  - **Only a number in article Name is not a valid unit. Default to appropriate "kg" or "stk"**`;
};
