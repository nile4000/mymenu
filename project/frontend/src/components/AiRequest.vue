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
  upsertArticleUnit,
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
  categorizationPrompt,
  validateExtractedCategories,
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
    const systemPrompt = ref<string>("");

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

    const detailExtractionPrompt = (batch: any[]) => {
      systemPrompt.value =
        "You are an text-extraction assistant. Provide only valid JSON strictlyin the format [{id: string, unit: string}] without any additional text or formatting.";
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

    const sendCategorizationRequest = async () => {
      try {
        validateSelectedItems();
        const preparedArticles = prepareArticles(props.selectedItems);
        const batches = createBatches(preparedArticles, 40);
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
          "gpt-4o-mini-2024-07-18",
          systemPrompt
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
        const batches = createBatches(preparedArticles, 40);

        isLoading.value = showLoading("Einheit extrahieren läuft...", $q);

        await processAllBatches(
          batches,
          detailExtractionPrompt,
          async (extractedDetails: any[]) => {
            const validExtractedDetails =
              validateExtractedDetails(extractedDetails);
            if (validExtractedDetails.length > 0) {
              await upsertArticleUnit(validExtractedDetails);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen extrahierten Einheiten gefunden.",
              });
              throw new Error("Ungültige extrahierte Einheiten.");
            }
          },
          "gpt-4o-mini-2024-07-18",
          systemPrompt
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
