<template>
  <div class="q-pa-md">
    <FoodTotal
      :totalsPerCategory="totalsPerCategory"
      :totalsPerReceipt="totalsPerReceipt"
      :totalCalculated="calculatedTotal"
      :totalExpenses="totalExpenses"
      :rows="rows"
    />

    <AiRequest :selectedItems="selected" />

    <q-table
      flat
      bordered
      title="Alle Esswaren"
      :rows="rows"
      :columns="columns"
      row-key="id"
      separator="cell"
      :pagination="initialPagination"
      v-model:selected="selected"
      selection="multiple"
    >
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
import { Column } from "../helpers/interfaces/column.interface";
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

export default defineComponent({
  name: "FoodPage",
  components: {
    AiRequest,
    FoodTotal,
  },
  setup() {
    const selected = ref<Article[]>([]);
    const rows = reactive<Article[]>([]);
    const receipts = reactive<Record<string, Receipt>>({});
    const message = ref("");
    const messageType = ref("positive");

    const columns: Column[] = [
      {
        name: "Purchase_Date",
        label: "Kaufdatum",
        field: "Purchase_Date",
        sortable: true,
        align: "center",
      },
      {
        name: "Category",
        label: "Kategorie",
        field: "Category",
        sortable: true,
        align: "center",
      },
      {
        name: "Name",
        required: true,
        label: "Name",
        align: "left",
        field: "Name",
        sortable: true,
      },
      {
        name: "Price",
        align: "center",
        label: "Preis",
        field: "Price",
        sortable: true,
      },
      { name: "Quantity", label: "Menge", field: "Quantity", sortable: true },
      {
        name: "Total",
        label: "Total (nach Rabatt)",
        field: "Total",
        sortable: true,
      },
    ];

    const initialPagination = ref({
      sortBy: "desc",
      descending: false,
      page: 1,
      rowsPerPage: 100,
    });

    const totalsPerCategory = computed(() => {
      return rows.reduce((acc, article) => {
        const category = article.Category;
        if (category) {
          if (!acc[category]) {
            acc[category] = 0;
          }
          acc[category] += parseFloat(article.Total.toString());
        }
        return acc;
      }, {} as Record<string, number>);
    });

    const totalsPerReceipt = computed(() => {
      return Object.values(receipts).map((receipt) => ({
        id: receipt.Id || "",
        date: receipt.Purchase_Date,
        total: parseFloat(receipt.Total_Receipt.toString()),
      }));
    });

    const totalExpenses = computed(() => {
      return Object.values(receipts).reduce((sum, receipt) => {
        return sum + parseFloat(receipt.Total_Receipt.toString());
      }, 0);
    });

    const calculatedTotal = computed(() => {
      return rows.reduce(
        (sum, item) => sum + parseFloat(item.Total.toString()),
        0
      );
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
        default:
          console.warn(`Unknown event type: ${eventType}`);
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
        console.error("Error loading articles from Supabase:", error);
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
            .filter((id): id is string => !!id) // Typ-Guard hinzugefügt
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
          console.error("Error loading receipts:", error);
        }
      }
    };

    return {
      columns,
      rows,
      selected,
      message,
      messageType,
      initialPagination,
      totalsPerCategory,
      totalsPerReceipt,
      totalExpenses,
      calculatedTotal,
    };
  },
});
</script>

<style scoped>
.q-mt-md {
  margin-top: 16px;
}
.q-mb-md {
  margin-bottom: 16px;
}
</style>
