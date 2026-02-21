<template>
  <q-btn-group rounded>
    <q-btn
      unelevated
      rounded
      :disabled="selectedItems.length === 0 || isLoading"
      @click="sendCategorizationRequest"
      v-ripple
    >
      <q-icon size="1.9em" name="replay" color="secondary" />
      <q-tooltip class="text-h6"> Klassifikation: {{ selectedItems.length }}</q-tooltip>
    </q-btn>
    <q-btn
      unelevated
      rounded
      @click="removeArticle(selectedItems[0].Id)"
      :disabled="selectedItems.length !== 1"
      v-ripple
    >
      <q-icon size="1.9em" name="delete" color="negative" />
      <q-tooltip anchor="center left" class="text-h6">Artikel löschen</q-tooltip>
    </q-btn>
  </q-btn-group>
</template>

<script lang="ts">
import { useQuasar } from "quasar";
import { defineComponent, PropType, ref } from "vue";
import { handleError } from "../helpers/composables/useErrors";
import { hideLoading, showLoading } from "../helpers/composables/useLoader";
import { Article } from "../helpers/interfaces/article.interface";
import { CategorizeResultItem, ExtractUnitResultItem } from "../services/ai/api/aiContracts";
import {
  categorizeArticles,
  deleteArticle,
  extractArticleUnits,
  updateArticleCategories,
  updateArticleUnits,
} from "../services";

export default defineComponent({
  name: "CategorizationRequest",
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  emits: ["article-deleted"],
  setup(props, { emit }) {
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

    const sendCategorizationRequest = async () => {
      try {
        validateSelectedItems();
        isLoading.value = showLoading("Kategorisierung läuft...", $q);

        const result = await categorizeArticles(props.selectedItems, validateAndUpdateCategory);
        if (!result.ok) {
          handleError("Kategorisierung", result.error.message, $q);
        }
      } catch (error) {
        handleError("Kategorisierung", error, $q);
      } finally {
        isLoading.value = hideLoading($q);
        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
        await sendDetailExtractionRequest();
      }
    };

    async function validateAndUpdateCategory(categorizedArticles: CategorizeResultItem[]): Promise<void> {
      if (categorizedArticles.length > 0) {
        const result = await updateArticleCategories(categorizedArticles);
        if (!result.ok) {
          handleError("Kategorisierung", result.error.message, $q);
        }
      } else {
        $q.notify({
          type: "warning",
          message: "Keine gültigen kategorisierten Artikel gefunden.",
        });
        throw new Error("Ungültige extrahierte Einheiten.");
      }
    }

    const sendDetailExtractionRequest = async () => {
      try {
        validateSelectedItems();
        isLoading.value = showLoading("Einheit extrahieren läuft...", $q);

        const result = await extractArticleUnits(props.selectedItems, validateAndUpdateUnitExtraction);
        if (!result.ok) {
          handleError("Einheitsextraktion", result.error.message, $q);
        }
      } catch (error) {
        handleError("Einheitsextraktion", error, $q);
      } finally {
        isLoading.value = hideLoading($q);
        $q.notify({
          type: "positive",
          message: "Einheitsextraktion erfolgreich!",
        });
      }
    };

    async function validateAndUpdateUnitExtraction(extractedDetails: ExtractUnitResultItem[]): Promise<void> {
      if (extractedDetails.length > 0) {
        const result = await updateArticleUnits(extractedDetails);
        if (!result.ok) {
          handleError("Einheitsextraktion", result.error.message, $q);
        }
      } else {
        $q.notify({
          type: "warning",
          message: "Keine gültigen extrahierten Einheiten gefunden.",
        });
        throw new Error("Ungültige extrahierte Einheiten.");
      }
    }

    const removeArticle = async (id: string) => {
      const result = await deleteArticle(id);
      if (!result.ok) {
        handleError("Artikel löschen", result.error.message, $q);
        return;
      }
      emit("article-deleted", id);
      $q.notify({
        type: "positive",
        message: "Artikel gelöscht!",
      });
    };

    return {
      sendCategorizationRequest,
      sendDetailExtractionRequest,
      removeArticle,
      isLoading,
    };
  },
});
</script>

<style lang="scss" scoped>
.q-btn {
  height: 45px;
}

.q-icon {
  margin-right: 10px;
}
.q-btn {
  .q-icon {
    margin-right: 10px;
  }
}

.q-btn-group {
  position: fixed;
  bottom: 0;
  right: 25px;
  height: 50px;
  margin-bottom: 10px;
  z-index: 1000;
  background-color: $bar-background;
  border: 1px solid $primary;
}
</style>
