<template>
  <div>
    <div class="column items-center">
      <h5>Übersicht</h5>
    </div>
    <div class="row q-pa-md justify-evenly">
      <FoodControl :totalExpenses="totalExpenses" :rows="filteredRows" />
      <FoodTotal
        :totalsPerCategory="totalsPerCategory"
        :totalsPerReceipt="totalsPerReceipt"
        :totalCalculatedPerReceipt="calculatedTotalPerReceipt"
        @update:selectedReceipts="handleSelectedReceipts"
      />
      <CategorizationRequest :selectedItems="selected" />
    </div>
    <h5
      class="column items-center"
      style="margin-block-start: 10px; margin-bottom: 20px"
    >
      Artikel ({{ selected.length }} / {{ filteredRows.length }})
    </h5>
    <q-table
      flat
      bordered
      grid
      :rows="filteredRows"
      :columns="columns"
      row-key="Id"
      :pagination="initialPagination"
      v-model:selected="selected"
      selection="multiple"
      class="table-custom"
      no-data-label="Keine Daten gefunden / keine Belege eingeblendet"
    >
      <template v-slot:top>
        <div style="width: 100%">
          <q-input
            rounded
            dense
            debounce="400"
            v-model="search"
            outlined
            label="Kaufdatum, Name oder Kategorie"
          >
            <template v-slot:append>
              <q-icon name="search" />
            </template>
          </q-input>
        </div>
      </template>
      <template v-slot:item="props">
        <div
          class="q-pa-xs col-xs-12 col-sm-6 col-md-4 col-lg-3 grid-style-transition"
        >
          <q-card
            bordered
            flat
            dense
            :class="
              props.selected
                ? $q.dark.isActive
                  ? '$positive'
                  : 'bg-grey-2'
                : ''
            "
          >
            <q-card-section class="q-pa-xs row justify-between">
              <q-checkbox
                v-model="props.selected"
                checked-icon="radio_button_checked"
                unchecked-icon="radio_button_unchecked"
                :label="props.row.Name"
              />
              <q-icon
                :name="getCategoryIcon(props.row.Category)"
                :color="getCategoryColor(props.row.Category)"
                size="md"
                ><q-tooltip anchor="center left" class="text-h6">{{
                  props.row.Category
                }}</q-tooltip>
              </q-icon>
            </q-card-section>
            <q-list dense style="padding-bottom: 7px">
              <q-item
                v-for="col in props.cols.filter((col) => col.name !== 'desc')"
                :key="col.name"
              >
                <q-item-section>
                  <q-item-label style="font-weight: bold">{{
                    col.label
                  }}</q-item-label>
                </q-item-section>
                <q-item-section side>
                  <q-item-label caption>{{ col.value }}</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-card>
        </div>
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
import { Article } from "../../helpers/interfaces/article.interface";
import {
  readAllArticles,
  readReceiptsByIds,
} from "../../services/readAllArticles";
import {
  subscribeToArticleChanges,
  unsubscribeFromArticleChanges,
} from "../../services/realtimeArticles";
import FoodTotal from "./FoodTotal.vue";
import FoodControl from "./FoodControl.vue";
import CategorizationRequest from "../../components/CategorizationRequest.vue";
import { Receipt } from "../../helpers/interfaces/receipt.interface";
import { articleColumns } from "../../helpers/columns/articleColumns";
import { useTotals } from "../../helpers/composables/UseTotals";
import { useQuasar } from "quasar";
import {
  categories,
  categoryIcon,
} from "../../components/prompts/categorization";
import {
  upsertArticleCategory,
  upsertArticleUnit,
} from "../../services/updateArticle";
import { handleError } from "../../helpers/composables/UseErrors";

export default defineComponent({
  name: "FoodTable",
  components: {
    CategorizationRequest,
    FoodTotal,
    FoodControl,
  },
  setup() {
    const $q = useQuasar();

    const search = ref("");
    const selected = ref<Article[]>([]);
    const rows = reactive<Article[]>([]);
    const receipts = reactive<Record<string, Receipt>>({});
    const message = ref("");
    const messageType = ref("positive");

    let channel: any;

    const initialPagination = ref({
      sortBy: "desc",
      descending: false,
      page: 1,
      rowsPerPage: 500,
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
          break;
        default:
          $q.notify({
            type: "warning",
            message: `Unknown event type: ${eventType}`,
          });
      }
    };

    onMounted(async () => {
      try {
        const data = await readAllArticles();
        if (data) {
          rows.splice(0, rows.length, ...data);
          await fetchReceiptsForArticles(data);
        }
      } catch (error) {
        handleError("Belege laden", error, $q);
      }
      channel = subscribeToArticleChanges(handleArticleChange);
    });

    onUnmounted(() => {
      if (channel) {
        unsubscribeFromArticleChanges(channel);
      }
    });

    const updateCategory = async (article: Article) => {
      try {
        await upsertArticleCategory(article);
        $q.notify({
          type: "positive",
          message: "Kategorie aktualisiert",
        });
      } catch (error) {
        handleError("Kategorie aktualisieren", error, $q);
      }
    };

    const updateUnit = async (article: Article) => {
      try {
        await upsertArticleUnit(article.Id, article.Unit);
        $q.notify({
          type: "positive",
          message: "Einheit aktualisiert",
        });
      } catch (error) {
        handleError("Einheit aktualisieren", error, $q);
      }
    };

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
          handleError("Belege laden", error, $q);
        }
      }
    };

    const handleSelectedReceipts = (selectedIds: string[]) => {
      selectedReceiptIds.value = selectedIds;
    };

    const getCategoryIcon = (categoryName: string): string => {
      const category = categoryIcon.find((c) => c.name === categoryName);
      return category ? category.icon : "help_outline"; // "help_outline" als Standard-Icon
    };

    const getCategoryColor = (categoryName: string): string => {
      const category = categoryIcon.find((c) => c.name === categoryName);
      return category ? category.color : "primary";
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
      updateCategory,
      updateUnit,
      getCategoryIcon,
      getCategoryColor,
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
:deep(th) {
  font-size: 14px;
  color: $dark;
  font-weight: bold;
}

.q-card {
  border-radius: 15px;
  border: 1px solid $primary;
}

h5 {
  margin-block-start: 25px;
  margin-block-end: 5px;
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
