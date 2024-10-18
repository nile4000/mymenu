<template>
  <div class="q-pa-md">
    <FoodTotal
      :totalsPerCategory="totalsPerCategory"
      :totalsPerReceipt="totalsPerReceipt"
      :totalCalculatedPerReceipt="calculatedTotalPerReceipt"
      :totalExpenses="totalExpenses"
      :rows="filteredRows"
    />

    <AiRequest :selectedItems="selected" />
    <q-table
      flat
      bordered
      :rows="filteredRows"
      :columns="columns"
      row-key="Id"
      separator="cell"
      :pagination="initialPagination"
      v-model:selected="selected"
      selection="multiple"
      no-data-label="Keine Daten gefunden"
    >
      <template v-slot:top>
        <div style="width: 100%" class="row">
          <div class="col-12">
            <q-input
              dense
              debounce="400"
              color="primary"
              v-model="search"
              outlined
              label="Kaufdatum, Kategorie oder Name"
            >
              <template v-slot:append>
                <q-icon name="search" />
              </template>
            </q-input>
          </div>
        </div>
      </template>
      <template v-slot:header-selection="scope">
        <q-toggle v-model="scope.selected" />
      </template>

      <template v-slot:body-selection="scope">
        <q-toggle v-model="scope.selected" />
      </template>
      <!-- ToDo: Edit Category -->
      <!-- <template v-slot:body="props">
        <q-td :props="props">
          <q-popup-edit v-model="props.row.Category" v-slot="scope">
            <q-input
              v-model="scope.value"
              dense
              autofocus
              counter
              @keyup.enter="scope.set"
            />
          </q-popup-edit>
        </q-td>
      </template> -->
    </q-table>
  </div>
</template>

<script lang="ts">
import {
  defineComponent,
  ref,
  onMounted,
  onUnmounted,
  reactive,
  computed,
} from "vue";
import { Article } from "../helpers/interfaces/article.interface";
import {
  readAllArticles,
  readReceiptsByIds,
} from "../services/readAllArticles";
import {
  subscribeToArticleChanges,
  unsubscribeFromArticleChanges,
} from "../services/realtimeArticles";
import FoodTotal from "./FoodTotal.vue";
import AiRequest from "../components/AiRequest.vue";
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { articleColumns } from "../helpers/columns/articleColumns";
import { useTotals } from "../helpers/composables/UseTotals";
import { useQuasar } from "quasar";

export default defineComponent({
  name: "FoodPage",
  components: {
    AiRequest,
    FoodTotal,
  },
  setup() {
    const $q = useQuasar();

    const search = ref("");
    const selected = ref<Article[]>([]);
    const rows = reactive<Article[]>([]);
    const receipts = reactive<Record<string, Receipt>>({});
    const message = ref("");
    const messageType = ref("positive");

    const initialPagination = ref({
      sortBy: "desc",
      descending: false,
      page: 1,
      rowsPerPage: 100,
    });

    const {
      totalsPerCategory,
      totalsPerReceipt,
      totalExpenses,
      calculatedTotalPerReceipt,
    } = useTotals(rows, receipts);

    const filterFields = ["Name", "Purchase_Date", "Category"] as const;

    const filteredRows = computed(() => {
      const cleanedSearch = search.value.trim().toLowerCase();
      if (!cleanedSearch) return rows;

      return rows.filter((row) => {
        return filterFields.some((field) => {
          const fieldValue = row[field];
          return (
            fieldValue &&
            String(fieldValue).toLowerCase().includes(cleanedSearch)
          );
        });
      });
    });

    // Handler für Echtzeit-Änderungen
    const handleArticleChange = (payload: any) => {
      const newArticle = payload.new as Article;
      const eventType = payload.eventType;

      switch (eventType) {
        case "INSERT":
          rows.push(newArticle);
          void fetchReceiptsForArticles([newArticle]);
          break;

        case "DELETE":
          rows.splice(rows.indexOf(newArticle), 1);
          break;

        case "UPDATE":
          const updateIndex = rows.findIndex(
            (article) => article.Id === newArticle.Id
          );
          if (updateIndex !== -1) {
            rows[updateIndex] = newArticle;
          } else {
            rows.push(newArticle);
          }
          break;
        default:
          $q.notify({
            type: "warning",
            message: `Unknown event type: ${eventType}`,
          });
      }
    };

    let channel: any;

    onMounted(async () => {
      try {
        const data = await readAllArticles();
        if (data) {
          rows.splice(0, rows.length, ...data);
          await fetchReceiptsForArticles(data);
        }
      } catch (error) {
        $q.notify({
          type: "negative",
          message: "Fehler beim Laden der Artikel vom Backend.",
        });
      }
      channel = subscribeToArticleChanges(handleArticleChange);
    });

    onUnmounted(() => {
      if (channel) {
        unsubscribeFromArticleChanges(channel);
      }
    });

    const fetchReceiptsForArticles = async (articles: Article[]) => {
      const uniqueReceiptIds = [
        ...new Set(
          articles
            .map((article) => article.Receipt_Id)
            .filter((id): id is string => !!id)
        ),
      ];
      const idsToFetch = uniqueReceiptIds.filter((id) => !receipts[id]);
      if (idsToFetch.length > 0) {
        try {
          const fetchedReceipts = await readReceiptsByIds(idsToFetch);
          fetchedReceipts.forEach((receipt) => {
            if (receipt.Id) {
              receipts[receipt.Id] = receipt;
            }
          });
        } catch (error) {
          $q.notify({
            type: "negative",
            message: "Fehler beim Laden der Belege vom Backend.",
          });
        }
      }
    };

    return {
      columns: articleColumns,
      filteredRows,
      search,
      selected,
      message,
      messageType,
      initialPagination,
      totalsPerCategory,
      totalsPerReceipt,
      totalExpenses,
      calculatedTotalPerReceipt,
    };
  },
});
</script>

<style scoped>
q-table {
padding-top: 16px;
}
.q-mt-md {
  margin-top: 16px;
}
.q-mb-md {
  margin-bottom: 16px;
}
</style>
