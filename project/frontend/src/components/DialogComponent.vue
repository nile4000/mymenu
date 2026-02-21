<template>
  <q-dialog transition-show="rotate">
    <q-card>
      <q-card-section>
        <q-table
          flat
          bordered
          title="Artikel extrahiert"
          :rows="articles"
          :columns="columns"
          row-key="Name"
          no-data-label="Keine Daten gefunden"
        >
        </q-table>
      </q-card-section>
      <q-card-section class="q-pt-none" side>
        <q-toggle
          v-model="performCategorization"
          label="Kategorisieren"
        ></q-toggle>
        <q-toggle
          v-model="performUnitExtraction"
          label="Einheiten extrahieren"
        ></q-toggle>
      </q-card-section>
      <q-card-actions align="right">
        <q-btn flat label="Abbrechen" color="primary" v-close-popup></q-btn>
        <q-btn
          flat
          label="Speichern"
          color="primary"
          v-close-popup
          @click="saveAll"
        ></q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import { QVueGlobals, useQuasar } from "quasar";
import { computed, defineComponent, PropType, ref } from "vue";
import { handleError } from "../helpers/composables/useErrors";
import { hideLoading, showLoading } from "../helpers/composables/useLoader";
import { Column } from "../helpers/interfaces/column.interface";
import { Article } from "../helpers/interfaces/article.interface";
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { ResponseItem } from "../helpers/interfaces/response-item.interface";
import { CategorizeResultItem, ExtractUnitResultItem } from "../services/ai/api/aiContracts";
import {
  categorizeArticles,
  extractArticleUnits,
  parseExtractResponse,
  saveReceiptWithArticles,
  updateArticleCategories,
  updateArticleUnits,
} from "../services";

const columns: Column[] = [
  {
    name: "Name",
    label: "Name",
    align: "left",
    field: "Name",
    sortable: true,
  },
  { name: "Menge", label: "Menge", field: "Quantity", sortable: true },
  { name: "Total", label: "Gesamt", field: "Total", sortable: true },
];

export default defineComponent({
  props: {
    response: {
      type: Array as PropType<ResponseItem[]>,
      required: true,
    },
  },

  setup(props, { emit }) {
    const isLoading = ref(false);
    const responseItem = computed<ResponseItem | null>(
      () => props.response?.[0] ?? null
    );
    const emptyReceipt: Receipt = {
      Purchase_Date: "",
      Corp: "Unknown",
      Total_Receipt: 0,
    };
    const normalizedResponse = computed(() => {
      if (!responseItem.value) {
        return { articles: [] as Article[], receipt: emptyReceipt };
      }
      return parseExtractResponse(responseItem.value);
    });

    const articles = computed(() => normalizedResponse.value.articles);
    const performCategorization = ref(true);
    const performUnitExtraction = ref(false);

    const $q: QVueGlobals = useQuasar();

    const receiptData = computed(() => normalizedResponse.value.receipt);

    const saveAll = async () => {
      try {
        if (!articles.value.length) {
          handleError("Speichern", "Keine Artikel in der Antwort gefunden.", $q);
          return;
        }
        emit("save-selection");
        const result = await saveReceiptWithArticles(articles.value, receiptData.value);
        if (!result.ok) {
          handleError("Speichern", result.error.message, $q);
          return;
        }

        if (performCategorization.value) {
          await categorizeSavedArticles(result.data.articles as any[]);
        }
        if (performUnitExtraction.value) {
          await extractUnit(result.data.articles as any[]);
        }
      } catch (error) {
        handleError("Speichern", error, $q);
      }
    };

    const categorizeSavedArticles = async (
      savedArticles: { Id: string; Name: string; Quantity: number; Price: number }[]
    ) => {
      try {
        isLoading.value = showLoading("Kategorisierung läuft...", $q);

        const result = await categorizeArticles(savedArticles, async (categorizedArticles: CategorizeResultItem[]) => {
          if (categorizedArticles.length > 0) {
            const updateResult = await updateArticleCategories(categorizedArticles);
            if (!updateResult.ok) {
              handleError("Kategorisieren", updateResult.error.message, $q);
            }
          } else {
            handleError(
              "Kategorisieren",
              "Keine gueltigen kategorisierten Artikel gefunden.",
              $q
            );
          }
        });

        if (!result.ok) {
          handleError("Kategorisieren", result.error.message, $q);
        }
      } catch (error) {
        handleError("Kategorisieren", error, $q);
      } finally {
        isLoading.value = hideLoading($q);
        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
      }
    };

    const extractUnit = async (
      savedArticles: { Id: string; Name: string; Quantity: number; Price: number }[]
    ) => {
      try {
        isLoading.value = showLoading("Einheit extrahieren laeuft...", $q);

        const result = await extractArticleUnits(savedArticles, async (extractedDetails: ExtractUnitResultItem[]) => {
          if (extractedDetails.length > 0) {
            const updateResult = await updateArticleUnits(extractedDetails);
            if (!updateResult.ok) {
              handleError("Einheitsextraktion", updateResult.error.message, $q);
            }
          } else {
            handleError(
              "Einheitsextraktion",
              "Keine gueltigen extrahierten Einheiten gefunden.",
              $q
            );
          }
        });

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

    return {
      articles,
      saveAll,
      columns,
      receiptData,
      performCategorization,
      performUnitExtraction,
      categorizeSavedArticles,
    };
  },
});
</script>

<style scoped lang="scss">
.q-table__card .q-table__top,
.q-table__card .q-table__bottom {
  background-color: #3fb7935f;
}

.ml-2 {
  margin-left: 0.5rem;
}

.text-subtitle1 {
  font-weight: 500;
}

.text-h6 {
  font-weight: 600;
}

.text-primary {
  color: #027be3;
}

.text-green-5 {
  color: #4caf50;
}

.text-blue-5 {
  color: #2196f3;
}
</style>
