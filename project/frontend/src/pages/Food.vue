<template>
  <div class="q-pa-md">
    <!-- Pass the rows to the FoodTotal component -->
    <FoodTotal
      :totalsPerCategory="totalsPerCategory"
      :totalsPerDate="totalsPerDate"
      :rows="rows"
    />

    <q-table
      flat
      bordered
      title="Alle Esswaren"
      :rows="rows"
      :columns="columns"
      row-key="id"
      separator="cell"
      :pagination="initialPagination"
    >
    </q-table>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, onUnmounted, reactive, computed } from "vue";
import { Article } from "../helpers/interfaces/article.interface";
import { Column } from "../helpers/interfaces/column.interface";
import { readAllArticles } from "../services/readAllArticles";
import {
  subscribeToArticleChanges,
  unsubscribeFromArticleChanges,
} from "../services/realtimeArticles";
import FoodTotal from "./FoodTotal.vue";

export default defineComponent({
  name: "FoodPage",
  components: {
    FoodTotal,
  },
  setup() {
    const selected = ref<Article[]>([]);
    const rows = reactive<Article[]>([]);
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
      // { name: "Discount", label: "Rabatt", field: "Discount", sortable: true },
      { name: "Total", label: "Total (nach Rabatt)", field: "Total", sortable: true },
    ];

    const initialPagination = ref({
      sortBy: "desc",
      descending: false,
      page: 1,
      rowsPerPage: 10,
    });

    // Computed property for totals per category
    const totalsPerCategory = computed(() => {
      return rows.reduce((acc, article) => {
        const category = article.Category;
        if (category) {
          if (!acc[category]) {
            acc[category] = 0;
          }
          acc[category] += article.Total;
        }
        return acc;
      }, {} as Record<string, number>);
    });

    // Computed property for totals per date
    const totalsPerDate = computed(() => {
      return rows.reduce((acc, article) => {
        const date = article.Purchase_Date;
        if (date) {
          if (!acc[date]) {
            acc[date] = 0;
          }
          acc[date] += article.Total;
        }
        return acc;
      }, {} as Record<string, number>);
    });

    const handleArticleChange = (payload: any) => {
      const newArticle = payload.new as Article;
      const eventType = payload.eventType;

      switch (eventType) {
        case "INSERT":
          rows.push(newArticle);
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
        }
      } catch (error) {
        console.error("Error loading articles from Supabase:", error);
      }

      // Realtime subscription
      channel = subscribeToArticleChanges(handleArticleChange);
    });

    onUnmounted(() => {
      if (channel) {
        unsubscribeFromArticleChanges(channel);
      }
    });

    return {
      columns,
      rows,
      selected,
      message,
      messageType,
      initialPagination,
      totalsPerCategory,
      totalsPerDate,
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
