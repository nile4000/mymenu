<template>
  <div>
    <section class="overview-block">
      <div class="column items-center">
        <h5>Übersicht</h5>
      </div>

      <div class="row q-pa-md justify-evenly overview-section">
        <ArticleControl :totalExpenses="totalExpenses" :totalsPerCategory="totalsPerCategory" :rows="filteredRows" />
        <ArticleTotal
          :totalsPerCategory="totalsPerCategory"
          :totalsPerReceipt="totalsPerReceipt"
        />
        <CategorizationRequest :selectedItems="selected" @article-deleted="onArticleDeleted" />
      </div>

      <h5 class="column items-center article-counter">
        Markiert: {{ selected.length }} von {{ filteredRows.length }} Artikeln
      </h5>
    </section>
    <div style="padding-bottom: 70px">
      <q-table
        flat
        bordered
        :grid="isGridView"
        :rows="filteredRows"
        :columns="isGridView ? columns : columnsList"
        row-key="Id"
        :pagination="initialPagination"
        :selected="selected"
        @update:selected="handleSelectionUpdate"
        selection="multiple"
        class="table-custom"
        :virtual-scroll="true"
        no-data-label="Keine Daten gefunden / Keine Kassenzettel eingeblendet"
      >
        <template v-slot:top>
          <div class="table-toolbar">
            <div class="table-top-controls">
              <div class="table-search">
                <TableSearchInput v-model="search" />
              </div>
              <q-btn-toggle
                v-model="viewMode"
                class="view-mode-toggle"
                no-caps
                unelevated
                rounded
                dense
                toggle-color="grey-4"
                color="grey-2"
                text-color="grey-8"
                :options="[
                  { label: 'Tabelle', value: 'table' },
                  { label: 'Grid', value: 'grid' },
                ]"
              />
            </div>
            <div class="quick-filter-row">
              <div class="quick-filter-chips">
                <q-chip
                  v-for="option in quickFilterOptions"
                  :key="option.value"
                  clickable
                  dense
                  :color="quickFilter === option.value ? 'primary' : 'grey-2'"
                  :text-color="quickFilter === option.value ? 'white' : 'grey-8'"
                  @click="quickFilter = option.value"
                >
                  {{ option.label }}
                </q-chip>
              </div>
              <div class="filter-stats-pill">
                <span class="filter-stats-count">Anzahl: {{ filteredRows.length }}</span>
                <span class="filter-stats-sep">·</span>
                <span class="filter-stats-total">{{ totalArticlesAmount.toFixed(2) }} CHF</span>
              </div>
            </div>
          </div>
        </template>

        <template v-if="isGridView" v-slot:item="props">
          <ArticleGrid
            :row="props.row"
            :cols="props.cols"
            :selected="props.selected"
            @update:selected="(val) => onItemSelected(props.row, val)"
          />
        </template>

        <template v-if="!isGridView" v-slot:body-cell-Category="props">
          <EditableCell
            :props="props"
            fieldName="Category"
            :categories="categories"
            :updateCategory="updateCategory"
            :updateUnit="updateUnit"
            :selectedItems="selected"
            :bulkUpdateCategory="bulkUpdateCategory"
          />
        </template>

        <template v-if="!isGridView" v-slot:body-cell-Unit="props">
          <EditableCell :props="props" fieldName="Unit" :updateUnit="updateUnit" :updateCategory="updateCategory" />
        </template>

        <template v-if="!isGridView" v-slot:body-cell-Total="props">
          <EditableCell :props="props" fieldName="Total" :updateTotal="updateTotal" />
        </template>
      </q-table>

    </div>
  </div>
</template>

<script lang="ts">
import { useQuasar } from "quasar";
import { storeToRefs } from "pinia";
import { computed, defineComponent, onMounted, ref, watch } from "vue";
import CategorizationRequest from "../../components/CategorizationRequest.vue";
import { articleColumns, articleColumnsList } from "../../helpers/columns/articleColumns";
import { handleError } from "../../helpers/composables/useErrors";
import { useFilters } from "../../helpers/composables/useFilters";
import { useTotals } from "../../helpers/composables/useTotals";
import { useViewMode } from "../../helpers/composables/useViewMode";
import { Article } from "../../helpers/interfaces/article.interface";
import { useDataStore } from "../../stores/dataStore";
import { useCategoryStore } from "../../stores/categoryStore";
import {
  updateArticleCategories,
  updateArticleCategory,
  updateArticleTotal,
  updateArticleUnit,
} from "../../services";
import EditableCell from "./EditableCell.vue";
import ArticleControl from "./ArticleControl.vue";
import ArticleGrid from "./ArticleGrid.vue";
import ArticleTotal from "./ArticleTotal.vue";
import TableSearchInput from "src/components/TableSearchInput.vue";
import { ArticleQuickFilter } from "src/stores/filterStore";

const quickFilterOptions: { label: string; value: ArticleQuickFilter }[] = [
  { label: "Alle", value: "all" },
  { label: "Artikel", value: "articles" },
  { label: "Korrekturen", value: "adjustments" },
  { label: "Minus", value: "negative" },
  { label: "Ohne Kategorie", value: "uncategorized" },
  { label: "Ohne Einheit", value: "noUnit" },
];

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
    const categoryStore = useCategoryStore();
    const { articles: rows, receiptById } = storeToRefs(dataStore);
    const { names: categories } = storeToRefs(categoryStore);

    // Filter-Logik
    const { search, quickFilter, filteredRows } = useFilters(rows);

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
      rowsPerPage: 25,
    });

    const { totalsPerCategory, totalsPerReceipt, totalExpenses } = useTotals(rows, receiptById);

    const totalArticlesAmount = computed(() =>
      filteredRows.value.reduce((sum, a) => sum + (a.Total ?? 0), 0)
    );

    watch(filteredRows, (newRows) => {
      const idSet = new Set(newRows.map((r) => r.Id));
      selected.value = selected.value.filter((item) => idSet.has(item.Id));
    });

    function onArticleDeleted(id: string) {
      selected.value = selected.value.filter((item) => item.Id !== id);
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
      const result = await updateArticleCategory(article);
      if (!result.ok) {
        handleError("Kategorie aktualisieren", result.error.message, $q);
      }
    }

    async function bulkUpdateCategory(articles: Article[], category: string) {
      const payload = articles
        .filter((a) => a.Id)
        .map((a) => ({ id: a.Id, category }));
      const result = await updateArticleCategories(payload);
      if (!result.ok) {
        handleError("Kategorien aktualisieren", result.error.message, $q);
      }
    }

    async function updateUnit(article: Article) {
      const result = await updateArticleUnit(article.Id, article.Unit);
      if (!result.ok) {
        handleError("Einheit aktualisieren", result.error.message, $q);
      }
    }

    async function updateTotal(article: Article) {
      const result = await updateArticleTotal(article.Id, article.Total);
      if (!result.ok) {
        handleError("Total aktualisieren", result.error.message, $q);
      }
    }

    onMounted(async () => {
      try {
        await Promise.all([
          dataStore.ensureInitialized(),
          categoryStore.ensureLoaded(),
        ]);
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
      quickFilter,
      quickFilterOptions,
      selected,
      initialPagination,
      totalsPerCategory,
      totalsPerReceipt,
      totalExpenses,
      totalArticlesAmount,
      categories,
      onItemSelected,
      onArticleDeleted,
      updateCategory,
      bulkUpdateCategory,
      updateUnit,
      updateTotal,
      isGridView,
      viewMode,
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
  border: 1px solid rgba(34, 46, 87, 0.2);
  border-radius: 999px;
  background-color: rgba(255, 255, 255, 0.6);
  padding: 2px;
  flex: 0 0 auto;
}

.view-mode-toggle :deep(.q-btn) {
  min-width: 76px;
  font-weight: 500;
  height: 34px;
}

.table-custom :deep(.q-table__top) {
  width: 100%;
}

.table-top-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.table-toolbar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.table-search {
  flex: 1 1 280px;
  min-width: 0;
}

.quick-filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  flex-wrap: wrap;
}

.quick-filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.quick-filter-chips :deep(.q-chip) {
  margin: 0;
}

.filter-stats-pill {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.04);
  font-size: 12px;
  white-space: nowrap;
  flex-shrink: 0;
}

.filter-stats-count {
  font-weight: 600;
  color: $dark;
}

.filter-stats-sep {
  color: #bbb;
}

.filter-stats-total {
  font-weight: 600;
  color: $primary;
}

@media (max-width: 600px) {
  .table-top-controls {
    align-items: stretch;
    flex-direction: column;
  }

  .view-mode-toggle {
    align-self: flex-end;
  }
}
</style>

