<template>
  <div>
    <section class="overview-block">
    <div class="column items-center">
      <h5>Übersicht</h5>
    </div>

    <div class="row q-pa-md justify-evenly overview-section">
      <ArticleControl :totalExpenses="totalExpenses" :rows="filteredRows" />
      <ArticleTotal
        :totalsPerCategory="totalsPerCategory"
        :totalsPerReceipt="totalsPerReceipt"
        :totalCalculatedPerReceipt="calculatedTotalPerReceipt"
        @update:selectedReceipts="handleSelectedReceipts"
        @update:selectedCategory="handleSelectedCategory"
      />
      <CategorizationRequest
        :selectedItems="selected"
        @article-deleted="onArticleDeleted"
      />
    </div>

    <div class="column items-center">
      <q-btn-toggle
        v-model="viewMode"
        class="view-mode-toggle"
        no-caps
        unelevated
        rounded
        toggle-color="primary"
        color="white"
        text-color="primary"
        :options="[
          { label: 'Grid', value: 'grid' },
          { label: 'Tabelle', value: 'table' },
        ]"
      />
    </div>

    <h5 class="column items-center article-counter">
      Ausgewählt: {{ selected.length }} von {{ filteredRows.length }} Artikeln
    </h5>

    </section>
    <div v-if="isGridView" style="padding-bottom: 70px">
      <!-- Grid Ansicht -->
      <q-table
        flat
        bordered
        grid
        :rows="filteredRows"
        :columns="columns"
        row-key="Id"
        :pagination="initialPagination"
        :selected="selected"
        @update:selected="handleSelectionUpdate"
        selection="multiple"
        class="table-custom"
        no-data-label="Keine Daten gefunden / keine Kassenzettel eingeblendet"
      >
        <template v-slot:top>
          <TableSearchInput v-model="search" />
        </template>

        <template v-slot:item="props">
          <ArticleGrid
            :row="props.row"
            :cols="props.cols"
            :selected="props.selected"
            @update:selected="(val) => onItemSelected(props.row, val)"
          />
        </template>
      </q-table>
    </div>

    <div v-else style="padding-bottom: 70px">
      <!-- Tabellenansicht -->
      <q-table
        flat
        bordered
        :rows="filteredRows"
        :columns="columnsList"
        row-key="Id"
        :pagination="initialPagination"
        :selected="selected"
        @update:selected="handleSelectionUpdate"
        selection="multiple"
        class="table-custom"
        no-data-label="Keine Daten gefunden / keine Kassenzettel eingeblendet"
      >
        <template v-slot:top>
          <TableSearchInput v-model="search" />
        </template>

        <!-- Toggles styled -->
        <template v-slot:header-selection="scope">
          <q-toggle v-model="scope.selected" />
        </template>
        <template v-slot:body-selection="scope">
          <q-toggle v-model="scope.selected" />
        </template>

        <!-- Editable columns -->
        <template v-slot:body-cell-Category="props">
          <EditableCell
            :props="props"
            fieldName="Category"
            :categories="categories"
            :updateCategory="updateCategory"
            :updateUnit="updateUnit"
          />
        </template>

        <template v-slot:body-cell-Unit="props">
          <EditableCell
            :props="props"
            fieldName="Unit"
            :updateUnit="updateUnit"
            :updateCategory="updateCategory"
          />
        </template>
      </q-table>
    </div>
  </div>
</template>

<script lang="ts">
import { useQuasar } from "quasar";
import { storeToRefs } from "pinia";
import { computed, defineComponent, onMounted, ref } from "vue";
import CategorizationRequest from "../../components/CategorizationRequest.vue";
import { categories } from "../../components/prompts/categorization";
import {
  articleColumns,
  articleColumnsList,
} from "../../helpers/columns/articleColumns";
import { handleError } from "../../helpers/composables/useErrors";
import { useFilters } from "../../helpers/composables/useFilters";
import { useTotals } from "../../helpers/composables/useTotals";
import { useViewMode } from "../../helpers/composables/useViewMode";
import { Article } from "../../helpers/interfaces/article.interface";
import { useDataStore } from "../../stores/dataStore";
import { upsertArticleCategory, upsertArticleUnit } from "../../services/updateArticle";
import EditableCell from "./EditableCell.vue";
import ArticleControl from "./ArticleControl.vue";
import ArticleGrid from "./ArticleGrid.vue";
import ArticleTotal from "./ArticleTotal.vue";
import TableSearchInput from "src/components/TableSearchInput.vue";

export default defineComponent({
  name: "ArticleTable",
  components: {
    ArticleControl,
    ArticleTotal,
    CategorizationRequest,
    ArticleGrid,
    EditableCell,
    TableSearchInput,
  },
  setup() {
    const $q = useQuasar();
    const dataStore = useDataStore();
    const { articles: rows, receiptById } = storeToRefs(dataStore);

    // Filter-Logik
    const { search, selectedCategory, selectedReceiptIds, filteredRows } =
      useFilters(rows);

    // View-Modus (Grid/Tabelle)
    const { isGridView } = useViewMode();
    const viewMode = computed<"grid" | "table">({
      get: () => (isGridView.value ? "grid" : "table"),
      set: (value) => {
        isGridView.value = value === "grid";
      },
    });

    const selected = ref<Article[]>([]);

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
    } = useTotals(rows, receiptById);

    function onArticleDeleted(id: string) {
      selected.value = selected.value.filter((item) => item.Id !== id);
    }

    function handleSelectedReceipts(ids: string[]) {
      selectedReceiptIds.value = ids;
    }

    function handleSelectedCategory(cat: string | null) {
      selectedCategory.value = cat;
    }

    function onItemSelected(row: Article, isSelected: boolean) {
      if (isSelected && !selected.value.includes(row)) {
        selected.value.push(row);
      } else if (!isSelected) {
        selected.value = selected.value.filter((item) => item.Id !== row.Id);
      }
    }
    function handleSelectionUpdate(updated: Article[]) {
      selected.value = updated;
    }

    async function updateCategory(article: Article) {
      try {
        await upsertArticleCategory(article);
      } catch (error) {
        handleError("Kategorie aktualisieren", error, $q);
      }
    }

    async function updateUnit(article: Article) {
      try {
        await upsertArticleUnit(article.Id, article.Unit);
      } catch (error) {
        handleError("Einheit aktualisieren", error, $q);
      }
    }

    onMounted(async () => {
      try {
        await dataStore.ensureInitialized();
      } catch (error) {
        handleError("Daten laden", error, $q);
      }
      dataStore.startRealtime();
    });

    return {
      columnsList: articleColumnsList,
      columns: articleColumns,
      filteredRows,
      search,
      selected,
      initialPagination,
      totalsPerCategory,
      totalsPerReceipt,
      totalExpenses,
      calculatedTotalPerReceipt,
      categories,
      onItemSelected,
      onArticleDeleted,
      handleSelectedReceipts,
      updateCategory,
      updateUnit,
      isGridView,
      viewMode,
      handleSelectedCategory,
      handleSelectionUpdate,
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

.overview-section {
  margin-bottom: 8px;
}

.article-counter {
  margin-block-start: 8px;
  margin-bottom: 4px;
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

.q-btn {
  height: 45px;
}

.view-mode-toggle {
  border: 1px solid $primary;
  border-radius: 999px;
  background-color: $bar-background;
  padding: 2px;
}

.view-mode-toggle :deep(.q-btn) {
  min-width: 90px;
  font-weight: 600;
}
</style>
