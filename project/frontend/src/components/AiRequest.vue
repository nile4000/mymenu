<template>
  <div>
    <q-btn
      color="primary"
      label="Beleg Übersicht"
      icon="dashboard"
      @click="openOverview"
      v-ripple
    ></q-btn>
    <q-btn
      color="negative"
      label="Artikel löschen"
      icon="delete"
      @click="deleteArticle(selectedItems[0].Id)"
      :disabled="selectedItems.length !== 1"
      v-ripple
    ></q-btn>
    <q-btn
      class="btn-categorize"
      color="secondary"
      :label="`Kategorisieren (${selectedItems.length})`"
      icon="menu_book"
      @click="sendCategorizationRequest"
      :disabled="selectedItems.length === 0 || isLoading"
      v-ripple
    ></q-btn>
    <q-btn
      class="btn-details"
      color="secondary"
      :label="`Einheit extrahieren (${selectedItems.length})`"
      icon="colorize"
      @click="sendDetailExtractionRequest"
      :disabled="selectedItems.length === 0 || isLoading"
      v-ripple
    ></q-btn>
  </div>
</template>

<script lang="ts">
import { Article } from "../helpers/interfaces/article.interface";
import router from "../router";
import { defineComponent, PropType, ref } from "vue";
import {
  upsertArticleCategories,
  upsertArticleDetails,
} from "../services/updateArticle";
import { useQuasar } from "quasar";
import {
  prepareArticles,
  createBatches,
  processAllBatches,
  prepareArticlesPrices,
} from "../services/aiRequest.service";
import { showLoading, hideLoading } from "../helpers/composables/UseLoader";
import { deleteArticleById } from "../services/deleteArticle";

export default defineComponent({
  name: "AiRequest",
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  setup(props) {
    const $q = useQuasar();
    const isLoading = ref(false);

    const validateSelectedItems = () => {
      if (props.selectedItems.length === 0) {
        $q.notify({
          type: "warning",
          message: "Bitte wählen Sie mindestens einen Artikel aus.",
        });
        throw new Error("Keine ausgewählten Artikel.");
      }
    };

    const handleError = (error: any) => {
      $q.notify({
        type: "negative",
        message:
          error.message ||
          "Ein unerwarteter Fehler ist bei der Anfrage aufgetreten.",
      });
    };

    const generateCategorizationPrompt = (batch: any[]) => {
      return `Categorize the following articles based on their names:\n${batch
        .map((article) => `ID: ${article.id}, Name: ${article.name}`)
        .join(
          "\n"
        )}\n\nUse the following categories for classification:\n1. Obst und Gemüse\n2. Milchprodukte, Eier und Alternativen\n3. Fleisch, Fisch und pflanzliche Proteine\n4. Backwaren und Getreide\n5. Softgetraenke\n6. Alkoholische/Alkoholfreie Getraenke\n7. Snacks, Apero und Suesswaren\n8. Reinigungsmittel und Haushaltsreiniger\n9. Koerperpflegeprodukte und Hygieneartikel\n10. Tierbedarf und Sonstiges\nIf an article does not fit into any of these categories, assign it the category 'Andere'. Use stritly the following format: [{id: string, category: string}].`;
    };

    const generateDetailExtractionPrompt = (batch: any[]) => {
      return `Extract unit information for the following articles based on their Name and Quantity:

${batch
  .map(
    (article) =>
      `Id: ${article.id}, Name: ${article.name}, Quantity: ${article.quantity}, Price: ${article.price}`
  )
  .join("\n")}

For each article, provide the following information in JSON format:

- **id**: The article's Id as a **string**.
- **unit**: The extracted unit of measurement in **lowercase letters** ("g", "ml", "stk"). Extract the unit from **Name** else from **Quantity** as appropriate.
- **quantity_in_unit**: The actual quantity in the extracted unit. Extract this from **Name** else from **Quantity** as appropriate.
- **base_unit**: The scaling factor based on the unit:

  - **For Weight Units**:
    - "g" → 100 (to calculate per 100g)
  - **For Volume Units**:
    - "ml" → 100 (to calculate per 100ml)
  - **For Quantity Units**:
    - "stk" → 1

- **price_base_unit**: The price per base unit, calculated as follows:

  - **For Weight Units ("g")**:
    - Use **quantity_in_unit** in grams.
    - \`price_base_unit = (Price / quantity_in_unit) × base_unit\`
  - **For Volume Units ("ml")**:
    - Use **quantity_in_unit** in milliliters.
    - \`price_base_unit = (Price / quantity_in_unit) × base_unit\`
  - **For Quantity Units ("stk")**:
    - \`price_base_unit = Price / Quantity\`

**Rules:**
- **Units must be in lowercase letters** (e.g., "g", "ml", "stk").
- **IDs must be strings**, enclosed in quotes.
- **Ensure accurate calculations** for \`base_unit\` and \`price_base_unit\` based on the unit type.
- **Extract quantities** from the **Name** if not provided in the **Quantity** field. In that case, calculate the correct unit from the **Name** like 0.325 Quantity is 325g (g is extracted from Name).
- **If no unit is found**, default to "stk" with a \`base_unit\` of 1.
- **Use strictly the following output format**: \`[{id: string, unit: string, base_unit: number, price_base_unit: number}]\`.
- **Validate the results with the examples below**.

**Example Calculations:**

1. **Article Name**: "Catsan Hygiene plus 20L"
   - **Id**: "123"
   - **Extracted Unit**: "ml" (since 1L = 1000ml)
   - **Quantity_in_unit**: 20,000 (20L × 1000ml/L)
   - **Price**: 19.95
   - **Base Unit**: 100 (per 100ml)

   **Calculation:**
   \`\`\`
   price_base_unit = (Price / Quantity_in_unit) × Base Unit
   price_base_unit = (19.95 / 20000) × 100 = 0.09975
   \`\`\`

2. **Article Name**: "Balsamico Dressing 60ML"
   - **Id**: "456"
   - **Extracted Unit**: "ml"
   - **Quantity_in_unit**: 60
   - **Price**: 0.70
   - **Base Unit**: 100 (per 100ml)

   **Calculation:**
   \`\`\`
   price_base_unit = (0.70 / 60) × 100 ≈ 1.1667
   \`\`\`

3. **Article Name**: "Danone Actimel Classic 0% 10×100g"
   - **Id**: "555"
   - **Extracted Unit**: "g"
   - **Quantity_in_unit**: 1,000 (10 × 100g)
   - **Price**: 8.20
   - **Base Unit**: 100 (per 100g)

   **Calculation:**
   \`\`\`
   price_base_unit = (8.20 / 1000) × 100 = 0.8200
   \`\`\`

4. **Article Name**: "Q&P Eier Freiland Schweiz 10St 53g+"
   - **Id**: "678"
   - **Extracted Unit**: "stk"
   - **Quantity_in_unit**: 10
   - **Price**: 4.95
   - **Base Unit**: 1

   **Calculation:**
   \`\`\`
   price_base_unit = (4.95 / 10) × 1 = 0.495
   \`\`\`

5. **Article Name**: "Coop Pouletbrust 2St. ca. 330g"
   - **Id**: "789"
   - **Extracted Unit**: "g"
   - **Quantity_in_unit**: 330
   - **Price**: 12.80
   - **Base Unit**: 100 (per 100g)

   **Calculation:**
   \`\`\`
   price_base_unit = (12.80 / 330) × 100 ≈ 3.8788
   \`\`\`

6. **Article Name**: "Calanda Radler Alpen Hugo 0.0 DS 6×33CL"
   - **Id**: "88"
   - **Extracted Unit**: "ml" (since 1cl = 10ml)
   - **Quantity_in_unit**: 1,980 (6 × 33cl × 10ml per cl)
   - **Price**: 8.50
   - **Base Unit**: 100 (per 100ml)

   **Calculation:**
   \`\`\`
   price_base_unit = (8.50 / 1980) × 100 ≈ 0.4293
   \`\`\`

**Example Output:**

\`\`\`json
[
  {
    "id": "123",
    "unit": "ml",
    "base_unit": 100,
    "price_base_unit": 0.09975
  },
  {
    "id": "456",
    "unit": "ml",
    "base_unit": 100,
    "price_base_unit": 1.1667
  },
  {
    "id": "555",
    "unit": "g",
    "base_unit": 100,
    "price_base_unit": 0.82
  },
  {
    "id": "678",
    "unit": "stk",
    "base_unit": 1,
    "price_base_unit": 0.495
  },
  {
    "id": "789",
    "unit": "g",
    "base_unit": 100,
    "price_base_unit": 3.8788
  },
  {
    "id": "88",
    "unit": "ml",
    "base_unit": 100,
    "price_base_unit": 0.4293
  }
]
\`\`\`

`;
    };

    const sendCategorizationRequest = async () => {
      try {
        validateSelectedItems();

        const preparedArticles = prepareArticles(props.selectedItems);
        const batches = createBatches(preparedArticles, 20);

        isLoading.value = showLoading("Kategorisierung läuft...", $q);

        await processAllBatches(
          batches,
          generateCategorizationPrompt,
          async (categorizedArticles: any[]) => {
            const validCategorizedArticles = categorizedArticles.filter(
              (article) =>
                article.id &&
                typeof article.id === "string" &&
                article.category &&
                typeof article.category === "string" &&
                article.category.trim() !== ""
            );

            if (validCategorizedArticles.length > 0) {
              await upsertArticleCategories(validCategorizedArticles);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen kategorisierten Artikel gefunden.",
              });
            }
          },
          "gpt-4o-2024-08-06"
        );

        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
      } catch (error) {
        handleError(error);
      } finally {
        isLoading.value = hideLoading($q);
      }
    };

    const sendDetailExtractionRequest = async () => {
      try {
        validateSelectedItems();

        const preparedArticles = prepareArticlesPrices(props.selectedItems);
        const batches = createBatches(preparedArticles, 20);

        isLoading.value = showLoading("Einheit extrahieren läuft...", $q);

        await processAllBatches(
          batches,
          generateDetailExtractionPrompt,
          async (extractedDetails: any[]) => {
            const validExtractedDetails = validateExtractedDetails(
              extractedDetails,
              $q
            );

            if (validExtractedDetails.length > 0) {
              await upsertArticleDetails(validExtractedDetails);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen extrahierten Einheiten gefunden.",
              });
            }
          },
          "gpt-4o-2024-08-06"
        );

        $q.notify({
          type: "positive",
          message: "Einheitsextraktion erfolgreich!",
        });
      } catch (error) {
        handleError(error);
      } finally {
        isLoading.value = hideLoading($q);
      }
    };

    const validateExtractedDetails = (
      extractedDetails: {
        id: string;
        unit: string;
        base_unit: number;
        price_base_unit: number;
      }[],
      $q: any
    ) => {
      const validExtractedDetails = extractedDetails.filter(
        (detail) =>
          detail.id &&
          typeof detail.id === "string" &&
          (typeof detail.unit === "string" || detail.unit === "")
      );

      if (validExtractedDetails.length === 0) {
        $q.notify({
          type: "warning",
          message: "Keine gültigen extrahierten Einheiten gefunden.",
        });
        throw new Error("Ungültige extrahierte Einheiten.");
      }

      return validExtractedDetails;
    };

    const openOverview = () => {
      void router.push("/receipt");
    };

    const deleteArticle = async (id: string) => {
      try {
        await deleteArticleById(id);
      } catch (error) {
        handleError(error);
      }
    };

    return {
      sendCategorizationRequest,
      sendDetailExtractionRequest,
      openOverview,
      deleteArticle,
      isLoading,
    };
  },
});
</script>

<style scoped>
.card-example {
  max-width: 400px;
  margin: 0 auto;
}
.button-loading-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
}
::v-deep .q-icon {
  color: white !important;
}

.btn-categorize {
  margin: 20px;
}

.btn-details {
  margin-bottom: 0px;
}

@media (max-width: 459px) {
  .btn-categorize {
    margin-top: 10px;
    margin-bottom: 10px;
    margin-left: 0;
  }
  .btn-details {
    margin-bottom: 10px;
  }
}
</style>
