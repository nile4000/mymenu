<template>
  <div>
    <q-btn
      color="primary"
      label="Beleg Übersicht"
      icon="dashboard"
      @click="openOverview"
      v-ripple
    ></q-btn>
    <!-- <q-btn
      color="negative"
      label="Artikel löschen"
      icon="delete"
      @click="deleteArticle(selectedItems[0].Id)"
      :disabled="selectedItems.length !== 1"
      v-ripple
    ></q-btn> -->
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
import {
  categories,
  formatArticlesForCategorization,
} from "./prompts/categorization";
import { formatArticlesForDetailExtraction } from "./prompts/detailExtraction";

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

    const categorizationPrompt = (batch: any[]) => {
      const formattedArticles = formatArticlesForCategorization(batch);
      const categoriesList = categories
        .map((category, index) => `${index + 1}. ${category}`)
        .join("\n");
      return `Categorize the following articles based on their names:/n
              ${formattedArticles}
              Use the following categories for classification:\n
              ${categoriesList}
              **Rules:**
              - If an article does not fit into any of these categories, assign it the category 'Andere'.
              - Use stritly the following jsonformat: [{id: string, category: string}].`;
    };

    const generateDetailExtractionPrompt = (batch: any[]) => {
      const formattedArticles = formatArticlesForDetailExtraction(batch);
      return `Extract unit information for the following articles based on their Name and Quantity:/n
              ${formattedArticles}
              For each article, provide the following information in JSON format:

              - **id**: The article's Id as a **string**, enclosed in quotes.
              - **unit**: The extracted unit from **Name** in **lowercase letters** ("g", "ml", "stk","cl","kg").

              **Rules:**
              - **If no unit is found in Name**, default to "stk" with a quantity of 1.
              - **Use strictly the following output format**: \`[{id: string, unit: string}]\`.
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
          categorizationPrompt,
          async (categorizedArticles: any[]) => {
            const validCategorizedArticles =
              validateExtractedCategories(categorizedArticles);
            if (validCategorizedArticles.length > 0) {
              await upsertArticleCategories(validCategorizedArticles);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen kategorisierten Artikel gefunden.",
              });
              throw new Error("Ungültige extrahierte Einheiten.");
            }
          },
          "gpt-4o-mini-2024-07-18"
        );
      } catch (error) {
        handleError(error);
      } finally {
        isLoading.value = hideLoading($q);
        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
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
            const validExtractedDetails =
              validateExtractedDetails(extractedDetails);
            if (validExtractedDetails.length > 0) {
              await upsertArticleDetails(validExtractedDetails);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen extrahierten Einheiten gefunden.",
              });
              throw new Error("Ungültige extrahierte Einheiten.");
            }
          },
          "gpt-4o-mini-2024-07-18"
        );
      } catch (error) {
        handleError(error);
      } finally {
        isLoading.value = hideLoading($q);
        $q.notify({
          type: "positive",
          message: "Einheitsextraktion erfolgreich!",
        });
      }
    };

    function validateExtractedCategories(
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

    const validateExtractedDetails = (
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
