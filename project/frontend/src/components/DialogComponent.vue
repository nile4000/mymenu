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
          label="Kategorisierung"
        ></q-toggle>
        <q-toggle
          v-model="performUnitExtraction"
          label="Einheit extrahieren"
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
import { computed, defineComponent, PropType, ref, Ref } from "vue";
import { handleError } from "../helpers/composables/UseErrors";
import { hideLoading, showLoading } from "../helpers/composables/UseLoader";
import { Column } from "../helpers/interfaces/column.interface";
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { ResponseItem } from "../helpers/interfaces/response-item.interface";
import {
  createBatches,
  prepareArticlesPrices,
  prepareDialogArticles,
  processAllBatches,
} from "../services/aiRequest.service";
import { saveArticlesAndReceipt } from "../services/saveArticles";
import {
  upsertArticleCategories,
  upsertArticleUnits,
} from "../services/updateArticle";
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
    const responseItem = computed(() => props.response?.[0] ?? {});

    const articles = computed(() => {
      const rawArticles =
        (responseItem.value as any).Articles ??
        (responseItem.value as any).articles ??
        [];

      if (!Array.isArray(rawArticles)) return [];

      return rawArticles.map((article: any) => ({
        Id: article.Id ?? article.id ?? undefined,
        Name: article.Name ?? article.name ?? "",
        Quantity: article.Quantity ?? article.quantity ?? 0,
        Price: article.Price ?? article.price ?? 0,
        Total:
          article.Total ??
          article.total ??
          article.Price ??
          article.price ??
          0,
        Discount: article.Discount ?? article.discount ?? 0,
        Category: article.Category ?? article.category ?? "",
        Unit: article.Unit ?? article.unit ?? undefined,
        Purchase_Date:
          article.Purchase_Date ??
          article.purchaseDate ??
          article.purchase_date ??
          undefined,
      }));
    });
    const performCategorization = ref(true);
    const performUnitExtraction = ref(false);

    const $q: QVueGlobals = useQuasar();

    const receiptData: Ref<Receipt> = computed(() => ({
      Uuid: (responseItem.value as any).UID ?? (responseItem.value as any).uid,
      Purchase_Date:
        (responseItem.value as any).Purchase_Date ??
        (responseItem.value as any).purchaseDate ??
        (responseItem.value as any).purchase_date ??
        "",
      Created_At:
        (responseItem.value as any).Created_At ??
        (responseItem.value as any).createdAt ??
        (responseItem.value as any).created_at,
      Corp:
        (responseItem.value as any).Corp ??
        (responseItem.value as any).corp ??
        "Unknown",
      Total_R_Extract:
        (responseItem.value as any).Total_R_Extract ??
        (responseItem.value as any).totalRExtract ??
        (responseItem.value as any).total_r_extract ??
        0,
      Total_R_Open_Ai:
        (responseItem.value as any).Total_R_Open_Ai ??
        (responseItem.value as any).totalROpenAi ??
        (responseItem.value as any).total_r_open_ai ??
        0,
      Total_Receipt: parseFloat(
        String(
          (responseItem.value as any).Total ??
            (responseItem.value as any).total ??
            0
        )
      ),
    }));

    const saveAll = async () => {
      try {
        if (!articles.value.length) {
          handleError(
            "Speichern",
            "Keine Artikel in der Antwort gefunden.",
            $q
          );
          return;
        }
        emit("save-selection");
        const result = await saveArticlesAndReceipt(
          articles.value,
          receiptData.value
        );
        if (performCategorization.value) {
          await categorizeArticles(result.articles);
        }
        if (performUnitExtraction.value) {
          await extractUnit(result.articles);
        }
      } catch (error) {
        handleError("Speichern", error, $q);
      }
    };

    const categorizeArticles = async (
      articles: { Id: string; Name: string; Quantity: number; Price: number }[]
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
              handleError(
                "Kategorisierung",
                "Keine gültigen kategorisierten Artikel gefunden.",
                $q
              );
            }
          },
          "gpt-4o-mini-2024-07-18",
          categorySystemPrompt
        );
      } catch (error) {
        handleError("Kategorisierung", error, $q);
      } finally {
        isLoading.value = hideLoading($q);
        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
      }
    };

    const extractUnit = async (
      articles: { Id: string; Name: string; Quantity: number; Price: number }[]
    ) => {
      try {
        const preparedArticles = prepareArticlesPrices(articles);
        const batches = createBatches(preparedArticles, 40);
        isLoading.value = showLoading("Einheit extrahieren läuft...", $q);

        await processAllBatches(
          batches,
          detailExtractionPrompt,
          async (extractedDetails) => {
            const validExtractedDetails =
              validateExtractedDetails(extractedDetails);
            if (validExtractedDetails.length > 0) {
              await upsertArticleUnits(validExtractedDetails);
            } else {
              handleError(
                "Kategorisierung",
                "Keine gültigen extrahierten Einheiten gefunden.",
                $q
              );
            }
          },
          "gpt-4o-mini-2024-07-18",
          detailSystemPrompt
        );
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
