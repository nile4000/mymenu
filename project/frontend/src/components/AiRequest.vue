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
  updateArticleCategoryById,
  updateArticleDetailsById,
} from "../services/updateArticle";
import { useQuasar } from "quasar";
import {
  prepareArticles,
  createBatches,
  processAllBatches,
  prepareArticlesPrices,
} from "../services/aiRequest.service";
import { showLoading, hideLoading } from "../helpers/composables/UseLoader";

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
        )}\n\nUse the following categories for classification:\n1. Obst und Gemüse\n2. Milchprodukte, Eier und Alternativen\n3. Fleisch, Fisch und pflanzliche Proteine\n4. Backwaren und Getreide\n5. Softgetraenke\n6. Alkoholische Getraenke\n7. Snacks, Apero und Suesswaren\n8. Reinigungsmittel und Haushaltsreiniger\n9. Koerperpflegeprodukte und Hygieneartikel\n10. Tierbedarf und Sonstiges\nIf an article does not fit into any of these categories, assign it the category 'Andere'. Use stritly the following format: [{id: string, category: string}].`;
    };

    const generateDetailExtractionPrompt = (batch: any[]) => {
      return `Extract unit information for the following articles based on their Name and Quantity:

${batch
  .map(
    (article) =>
      `ID: ${article.id}, Name: ${article.name}, Quantity: ${article.quantity}, Price: ${article.price}`
  )
  .join("\n")}

For each article, provide the following information in JSON format:

- **id**: The article's ID as a **string**.
- **unit**: The extracted unit of measurement in **lowercase letters** (e.g., g, kg, ml, l, stk). If no unit is found, default to "stk".
- **base_unit**: The scaling factor based on the unit:

  - **Weight Units**:
    - mg → 0.1 (per 100g)
    - g → 100 (per 100g)
    - kg → 1 (per 1kg)
  - **Volume Units**:
    - ml → 1 (per 100ml)
    - dl → 10 (per 100ml)
    - l → 1 (per 1L)
  - **Quantity Units**:
    - stk → 1

- **price_base_unit**: The price per base unit, calculated as:

  - **For Weight Units**:
    - \`price_base_unit = (Price / Quantity in grams) × base_unit\`
  - **For Volume Units**:
    - \`price_base_unit = (Price / Quantity in milliliters) × base_unit\`
  - **For Quantity Units (stk)**:
    - \`price_base_unit = Price / Quantity\`

**Rules:**

- **Units must be in lowercase letters** (e.g., "g", not "G").
- **IDs must be strings**, enclosed in quotes.
- **Ensure accurate calculations** for \`base_unit\` and \`price_base_unit\` based on the unit type.
- **If no unit is found**, default to "stk" with a \`base_unit\` of 1.
- **Validate JSON structure** to ensure it is correctly formatted.

**Example Output:**

\`\`\`json
[
  {
    "id": "123",
    "unit": "kg",
    "base_unit": 1,
    "price_base_unit": 5.00
  },
  {
    "id": "456",
    "unit": "ml",
    "base_unit": 1,
    "price_base_unit": 0.50
  },
  {
    "id": "789",
    "unit": "stk",
    "base_unit": 1,
    "price_base_unit": 2.00
  }
]
\`\`\`

`;
    };

    const sendCategorizationRequest = async () => {
      try {
        validateSelectedItems();

        const preparedArticles = prepareArticles(props.selectedItems);
        const batches = createBatches(preparedArticles, 50);

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
              await updateArticleCategoryById(validCategorizedArticles);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen kategorisierten Artikel gefunden.",
              });
            }
          },
          "gpt-4o-mini"
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
        const batches = createBatches(preparedArticles, 50);

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
              await updateArticleDetailsById(validExtractedDetails);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen extrahierten Einheiten gefunden.",
              });
            }
          },
          "gpt-3.5-turbo-0125"
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

    return {
      sendCategorizationRequest,
      sendDetailExtractionRequest,
      openOverview,
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
