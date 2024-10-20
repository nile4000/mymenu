<template>
  <q-dialog transition-show="rotate">
    <q-card>
      <q-card-section>
        <q-row>
          <!-- Commented sections -->
        </q-row>
      </q-card-section>
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
          label="Kategorisierung"
        ></q-toggle>
        <!-- <q-toggle
          v-model="performDetailExtraction"
          label="Einheit extrahieren"
        ></q-toggle> -->
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
import { defineComponent, ref, computed, PropType } from "vue";
import { Column } from "../helpers/interfaces/column.interface";
import { saveArticlesAndReceipt } from "../services/saveArticles";
import { ResponseItem } from "../helpers/interfaces/response-item.interface";
import {
  createBatches,
  prepareDialogArticles,
  processAllBatches,
} from "../services/aiRequest.service";
import { hideLoading, showLoading } from "../helpers/composables/UseLoader";
import { useQuasar } from "quasar";
import { upsertArticleCategories } from "../services/updateArticle";
import {
  categorizationPrompt,
  validateExtractedCategories,
} from "./prompts/categorization";

const columns: Column[] = [
  {
    name: "Name",
    required: true,
    label: "Name",
    align: "left",
    field: "Name",
    sortable: true,
  },
  {
    name: "Preis",
    align: "center",
    label: "Preis",
    field: "Price",
    sortable: true,
  },
  { name: "Menge", label: "Menge", field: "Quantity", sortable: true },
  { name: "Rabatt", label: "Aktion", field: "Discount", sortable: true },
  { name: "Total", label: "Gesamt", field: "Total", sortable: true },
  { name: "Kategorie", label: "Kategorie", field: "Category", sortable: true },
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
    const articles = computed(() => props.response[0].Articles);
    const performCategorization = ref(true);
    const systemPrompt = ref<string>("");

    const $q = useQuasar();

    const receiptData = computed(() => ({
      Uuid: props.response[0].UID,
      Purchase_Date: props.response[0].Purchase_Date,
      Corp: props.response[0].Corp,
      Total_R_Extract: props.response[0].Total_R_Extract,
      Total_R_Open_Ai: props.response[0].Total_R_Open_Ai,
      Total_Receipt: parseFloat(props.response[0].Total),
    }));

    const saveAll = async () => {
      try {
        emit("save-selection");
        const result = await saveArticlesAndReceipt(
          articles.value,
          receiptData.value
        );
        if (performCategorization.value) {
          await categorizeArticles(result.articles);
        }
      } catch (error: any) {
        console.error("Error saving selection:", error);
        $q.notify({
          type: "negative",
          message: "Fehler beim Speichern der Auswahl.",
        });
      }
    };

    const categorizeArticles = async (
      articles: { Id: string; Name: string }[]
    ) => {
      try {
        const preparedArticles = prepareDialogArticles(articles);
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
              throw new Error("Ungültige extrahierte Kategorien.");
            }
          },
          "gpt-4o-mini-2024-07-18",
          systemPrompt
        );
      } catch (error: any) {
        console.error("Error categorizing articles:", error);
        $q.notify({
          type: "negative",
          message: "Fehler bei der Kategorisierung der Artikel.",
        });
      } finally {
        isLoading.value = hideLoading($q);
        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
      }
    };

    return {
      articles,
      saveAll,
      columns,
      receiptData,
      performCategorization,
      categorizeArticles,
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
