<template>
  <div>
    <q-dialog
      v-model="showDialogRecipe"
      persistent
      transition-show="slide-up"
      transition-hide="slide-down"
    >
      <q-card class="custom-card">
        <q-card-section class="column items-center">
          <h5 style="margin-block-end: 20px">Einstellungen</h5>
          <q-badge color="secondary" class="q-mb-md">
            Anzahl Personen: {{ standard }} (0-10)
          </q-badge>
          <q-slider
            v-model="standard"
            :min="1"
            :max="10"
            style="max-width: 300px"
            color="secondary"
          />
        </q-card-section>
        <q-card-section style="margin-top: 0px">
          <q-expansion-item :dense="false" class="custom-card">
            <template v-slot:header>
              <q-item-section avatar>
                <q-avatar icon="category" class="colored-icon" />
              </q-item-section>
              <q-item-section style="font-weight: bold">
                Kategorie
              </q-item-section>
            </template>
            <q-list>
              <q-item v-for="item in categories" :key="item">
                <q-item-section>
                  {{ item }}
                </q-item-section>
              </q-item>
            </q-list>
          </q-expansion-item>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Abbrechen" color="negative" v-close-popup />
          <q-btn
            flat
            label="Erstellen"
            color="primary"
            style="font-weight: bold"
            v-close-popup
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
    <q-btn-group rounded>
      <q-btn
        unelevated
        rounded
        :disabled="isLoading"
        @click="showDialogRecipe = true"
        v-ripple
      >
        <q-icon size="1.9em" name="hub" color="secondary" />
        <div class="text-h6">Rezept</div>
      </q-btn>
    </q-btn-group>
  </div>
</template>

<script lang="ts">
import { Article } from "../helpers/interfaces/article.interface";
import { defineComponent, PropType, ref } from "vue";

import { useQuasar } from "quasar";

import { showLoading, hideLoading } from "../helpers/composables/UseLoader";
import { deleteArticleById } from "../services/deleteArticle";
import {
  categorizationPrompt,
  categorySystemPrompt,
  validateExtractedCategories,
} from "./prompts/categorization";
import {
  detailExtractionPrompt,
  detailSystemPrompt,
  validateExtractedDetails,
} from "./prompts/detailExtraction";
import { handleError } from "../helpers/composables/UseErrors";
import { categories } from "./prompts/receipe";

export default defineComponent({
  name: "RecipeRequest",
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  setup(props) {
    const $q = useQuasar();
    const isLoading = ref(false);
    const showDialogRecipe = ref(false);

    // const validateSelectedItems = () => {
    //   if (props.selectedItems.length === 0) {
    //     $q.notify({
    //       type: "warning",
    //       message: "Bitte wählen Sie mindestens einen Artikel aus.",
    //     });
    //     throw new Error("Keine ausgewählten Artikel.");
    //   }
    // };

    // const sendCategorizationRequest = async () => {
    //   try {
    //     validateSelectedItems();
    //     const preparedArticles = prepareArticles(props.selectedItems);
    //     const batches = createBatches(preparedArticles, 40);
    //     isLoading.value = showLoading("Kategorisierung läuft...", $q);

    //     await processAllBatches(
    //       batches,
    //       categorizationPrompt,
    //       validateAndUpdateCategory,
    //       "gpt-4o-mini-2024-07-18",
    //       categorySystemPrompt
    //     );
    //   } catch (error) {
    //     handleError("Kategorisierung", error, $q);
    //   } finally {
    //     isLoading.value = hideLoading($q);
    //     $q.notify({
    //       type: "positive",
    //       message: "Kategorisierung erfolgreich!",
    //     });
    //     // no further detail extraction
    //     await sendDetailExtractionRequest();
    //   }
    // };

    // async function validateAndUpdateCategory(
    //   categorizedArticles: any[]
    // ): Promise<void> {
    //   const validCategorizedArticles =
    //     validateExtractedCategories(categorizedArticles);
    //   if (validCategorizedArticles.length > 0) {
    //     await upsertArticleCategories(validCategorizedArticles);
    //   } else {
    //     $q.notify({
    //       type: "warning",
    //       message: "Keine gültigen kategorisierten Artikel gefunden.",
    //     });
    //     throw new Error("Ungültige extrahierte Einheiten.");
    //   }
    // }

    // const sendDetailExtractionRequest = async () => {
    //   try {
    //     validateSelectedItems();
    //     const preparedArticles = prepareArticlesPrices(props.selectedItems);
    //     const batches = createBatches(preparedArticles, 40);
    //     console.log(batches);

    //     isLoading.value = showLoading("Einheit extrahieren läuft...", $q);

    //     await processAllBatches(
    //       batches,
    //       detailExtractionPrompt,
    //       validateAndUpdateUnitExtraction,
    //       "gpt-4o-mini-2024-07-18",
    //       detailSystemPrompt
    //     );
    //   } catch (error) {
    //     handleError("Einheitsextraktion", error, $q);
    //   } finally {
    //     isLoading.value = hideLoading($q);
    //     $q.notify({
    //       type: "positive",
    //       message: "Einheitsextraktion erfolgreich!",
    //     });
    //   }
    // };

    // async function validateAndUpdateUnitExtraction(
    //   extractedDetails: any[]
    // ): Promise<void> {
    //   const validExtractedDetails = validateExtractedDetails(extractedDetails);
    //   if (validExtractedDetails.length > 0) {
    //     await upsertArticleUnits(validExtractedDetails);
    //   } else {
    //     $q.notify({
    //       type: "warning",
    //       message: "Keine gültigen extrahierten Einheiten gefunden.",
    //     });
    //     throw new Error("Ungültige extrahierte Einheiten.");
    //   }
    // }

    const deleteArticle = async (id: string) => {
      try {
        await deleteArticleById(id);
      } catch (error) {
        handleError("Artikel löschen", error, $q);
      }
    };

    return {
      deleteArticle,
      isLoading,
      showDialogRecipe,
      standard: ref(2),
      categories,
    };
  },
});
</script>

<style lang="scss" scoped>
.card-example {
  margin: 0 auto;
}

.custom-card {
  background-color: $bar-background;
  border-radius: 15px;
  border: 1px solid $dark;

  h5 {
    margin-block-start: 0px;
  }

  :deep(.q-focus-helper) {
    display: none;
  }
}

.colored-icon {
  :deep(.q-avatar__content) {
    .q-icon {
      color: $secondary !important;
    }
  }
}
.button-loading-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
}
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

.text-h6 {
  text-transform: none;
}

.q-btn-group {
  position: fixed;
  bottom: 0;
  right: 25px;
  margin-bottom: 10px;
  z-index: 1000;
  background-color: $bar-background;
  border: 1px solid $primary;
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
