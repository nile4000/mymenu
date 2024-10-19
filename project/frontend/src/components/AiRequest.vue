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
      return `Extract unit information for the following articles based on their names:\n${batch
        .map((article) => `ID: ${article.id}, Name: ${article.name}`)
        .join(
          "\n"
        )}\n\nFor each article, provide the unit and id in JSON format: [{id: string, unit: string}]. Default to empty string if no unit is found.`;
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
          }
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

        const preparedArticles = prepareArticles(props.selectedItems);
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
          }
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
      extractedDetails: { id: string; unit: string }[],
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
