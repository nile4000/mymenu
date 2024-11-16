<template>
  <div class="q-pa-md row items-start q-gutter-md">
    <FoodDasboard :totalExpenses="totalExpenses" :rows="filteredRows" />
    <FoodTotal
      :totalsPerCategory="totalsPerCategory"
      :totalsPerReceipt="totalsPerReceipt"
      :totalCalculatedPerReceipt="calculatedTotalPerReceipt"
      @update:selectedReceipts="handleSelectedReceipts"
    />
    <AiRequest :selectedItems="selected" />
    <q-table
      flat
      bordered
      :rows="filteredRows"
      :columns="columns"
      row-key="Id"
      :pagination="initialPagination"
      v-model:selected="selected"
      selection="multiple"
      class="table-custom"
      no-data-label="Keine Daten gefunden, keine Belege selektiert"
    >
      <template v-slot:top>
        <div style="width: 100%" class="row">
          <div
            class="row items-center justify-center q-gutter-sm"
            style="margin-bottom: 10px"
          >
            <q-icon size="1.4em" name="restaurant" color="negative" />
            <span class="text-h6" style="font-weight: bold">Artikel</span>
          </div>
          <div class="col-12">
            <q-input
              rounded
              dense
              debounce="400"
              v-model="search"
              outlined
              label="Kaufdatum, Name oder Kategorie"
            >
              <span></span>
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
      <template v-slot:body-cell-Category="props">
        <q-td :props="props">
          {{ props.row.Category }}
          <q-popup-edit v-model="props.row.Category" v-slot="scope">
            <q-select
              v-model="scope.value"
              :options="categories"
              dense
              autofocus
              @blur="scope.set"
              @keyup.enter="scope.set"
            />
          </q-popup-edit>
        </q-td>
      </template>
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
import FoodDasboard from "./FoodDashboard.vue";
import AiRequest from "../components/AiRequest.vue";
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { articleColumns } from "../helpers/columns/articleColumns";
import { useTotals } from "../helpers/composables/UseTotals";
import { useQuasar } from "quasar";
import { categories } from "../components/prompts/categorization";

export default defineComponent({
  name: "FoodPage",
  components: {
    AiRequest,
    FoodTotal,
    FoodDasboard,
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

    const selectedReceiptIds = ref<string[]>([]);

    const filteredRows = computed(() => {
      let filtered = rows;

      const cleanedSearch = search.value.trim().toLowerCase();
      if (cleanedSearch) {
        filtered = filtered.filter((row) => {
          return filterFields.some((field) => {
            const fieldValue = row[field];
            return (
              fieldValue &&
              String(fieldValue).toLowerCase().includes(cleanedSearch)
            );
          });
        });
      }

      if (selectedReceiptIds.value.length > 0) {
        filtered = filtered.filter(
          (row) =>
            row.Receipt_Id &&
            selectedReceiptIds.value.includes(String(row.Receipt_Id))
        );
      } else {
        filtered = [];
      }

      return filtered;
    });

    const handleArticleChange = (payload: any) => {
      const newArticle = payload.new as Article;
      const eventType = payload.eventType;

      switch (eventType) {
        case "INSERT":
          rows.push(newArticle);
          void fetchReceiptsForArticles([newArticle]);
          break;

        case "DELETE":
          // eslint-disable-next-line no-case-declarations
          const indexToDelete = rows.findIndex(
            (article: Article) => article.Id === newArticle.Id
          );
          if (indexToDelete !== -1) {
            rows.splice(indexToDelete, 1);
          }
          break;

        case "UPDATE":
          // eslint-disable-next-line no-case-declarations
          const updateIndex = rows.findIndex(
            (article: Article) => article.Id === newArticle.Id
          );
          if (updateIndex !== -1) {
            rows[updateIndex] = newArticle;
          } else {
            rows.push(newArticle);
          }
          // todo: submit
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

    const handleSelectedReceipts = (selectedIds: string[]) => {
      selectedReceiptIds.value = selectedIds;
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
      categories,
      handleSelectedReceipts,
      selectedReceiptIds,
    };
  },
});
</script>

<style lang="scss" scoped>
.table-custom {
  width: 100%;
  border-radius: 15px;
  border: 1px solid $primary;
}
thead {
  th {
    font-weight: bold;
  }
}

.q-input {
  border-radius: 15px;
  background-color: $bar-background;
}

.text-h6 {
  color: $dark;

}

.q-mt-md {
  margin-top: 16px;
}
.q-mb-md {
  margin-bottom: 16px;
}
</style>
