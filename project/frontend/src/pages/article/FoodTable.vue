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
        @update:selectedCategory="handleSelectedCategory"
      />
      <CategorizationRequest
        :selectedItems="selected"
        @article-deleted="onArticleDeleted"
      />
    </div>

    <div class="column items-center">
      <q-btn
        @click="toggleView"
        unelevated
        rounded
        :label="
          isGridView
            ? 'Zur Tabellenansicht wechseln'
            : 'Zur Grid-Ansicht wechseln'
        "
        color="primary"
      />
    </div>

    <h5
      class="column items-center"
      style="margin-block-start: 10px; margin-bottom: 20px"
    >
      Artikel ({{ selected.length }} / {{ filteredRows.length }})
    </h5>

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
        no-data-label="Keine Daten gefunden / keine Belege eingeblendet"
      >
        <template v-slot:top>
          <TableSearchInput v-model="search" />
        </template>

        <template v-slot:item="props">
          <FoodGrid
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
        no-data-label="Keine Daten gefunden / keine Belege eingeblendet"
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
import { defineComponent, onMounted, onUnmounted, ref } from "vue";
import CategorizationRequest from "../../components/CategorizationRequest.vue";
import { categories } from "../../components/prompts/categorization";
import {
  articleColumns,
  articleColumnsList,
} from "../../helpers/columns/articleColumns";
import { useArticles } from "../../helpers/composables/UseArticles";
import { handleError } from "../../helpers/composables/UseErrors";
import { useFilters } from "../../helpers/composables/UseFilters";
import { useTotals } from "../../helpers/composables/UseTotals";
import { useViewMode } from "../../helpers/composables/UseViewMode";
import { Article } from "../../helpers/interfaces/article.interface";
import EditableCell from "./EditableCell.vue";
import FoodControl from "./FoodControl.vue";
import FoodGrid from "./FoodGrid.vue";
import FoodTotal from "./FoodTotal.vue";
import TableSearchInput from "src/components/TableSearchInput.vue";

export default defineComponent({
  name: "FoodTable",
  components: {
    FoodControl,
    FoodTotal,
    CategorizationRequest,
    FoodGrid,
    EditableCell,
    TableSearchInput,
  },
  setup() {
    const $q = useQuasar();

    // Artikel laden & verwalten
    const {
      rows,
      receipts,
      loadArticles,
      subscribeToArticles,
      unsubscribeArticles,
      updateCategory,
      updateUnit,
    } = useArticles($q);

    // Filter-Logik
    const { search, selectedCategory, selectedReceiptIds, filteredRows } =
      useFilters(rows);

    // View-Modus (Grid/Tabelle)
    const { isGridView, toggleView } = useViewMode();

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
    } = useTotals(rows, receipts);

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

    onMounted(async () => {
      try {
        await loadArticles();
      } catch (error) {
        handleError("Belege laden", error, $q);
      }
      subscribeToArticles();
    });

    onUnmounted(() => {
      unsubscribeArticles();
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
      toggleView,
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
</style>
