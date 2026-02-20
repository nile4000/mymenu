<template>
  <q-list class="justify-center custom-list">
    <FilterPanel
      panel-class="my-card filter-panel"
      title="Filter: Kassenzettel"
      caption="Klickbar: blendet Artikel pro Kassenzettel ein/aus."
      icon="receipt_long"
      icon-class="colored-icon2"
      :badge-text="`${activeReceiptCount} aktiv`"
    >
      <template #controls>
        <div class="q-pa-sm">
          <q-select
            v-model="receiptSortCriteria"
            :options="[
              { label: 'Datum', value: 'Datum' },
              { label: 'Total', value: 'Total' },
            ]"
            label="Sortieren nach"
            dense
            outlined
            rounded
          />
        </div>
      </template>

      <ReceiptFilterList
        v-model="selectedReceiptIds"
        :options="receiptFilterItems"
        show-visibility-icon
        tooltip-text="Artikel pro Kassenzettel ein-/ausblenden"
      />
    </FilterPanel>

    <FilterPanel
      panel-class="my-card filter-panel"
      title="Filter: Kategorien"
      caption="Klickbar: filtert die Artikelliste nach Kategorie."
      icon="hub"
      icon-class="colored-icon"
      color="primary"
      :badge-text="categoryFilterStatus"
    >
      <template #controls>
        <div class="q-pa-sm">
          <q-select
            v-model="categorySortCriteria"
            :options="[
              { label: 'Name', value: 'Name' },
              { label: 'Total', value: 'Total' },
            ]"
            label="Sortieren nach"
            dense
            outlined
            rounded
          />
        </div>
      </template>

      <CategoryFilterList
        v-model="selectedCategory"
        :options="categoryFilterItems"
        show-all-option
      />
    </FilterPanel>
  </q-list>
</template>

<script lang="ts">
import { computed, defineComponent, PropType, ref, watch } from "vue";
import { categoryIcon } from "../../components/prompts/categorization";
import { formatDate } from "../../helpers/dateHelpers";
import FilterPanel from "../../components/filters/FilterPanel.vue";
import CategoryFilterList from "../../components/filters/CategoryFilterList.vue";
import ReceiptFilterList from "../../components/filters/ReceiptFilterList.vue";

export default defineComponent({
  name: "ArticleTotal",
  components: {
    FilterPanel,
    CategoryFilterList,
    ReceiptFilterList,
  },
  props: {
    totalsPerCategory: {
      type: Object as () => Record<string, number>,
      required: true,
    },
    totalsPerReceipt: {
      type: Array as PropType<Array<{ id: string; date: string; total: number }>>,
      required: true,
    },
  },
  emits: ["update:selectedReceipts", "update:selectedCategory"],
  setup(props, { emit }) {
    const selectedReceiptIds = ref<string[]>([]);
    const selectedCategory = ref<string | null>(null);
    const receiptSortCriteria = ref<"Datum" | "Total">("Datum");
    const categorySortCriteria = ref<"Name" | "Total">("Name");

    const categoryMetaByName = computed<Record<string, { icon: string; color: string }>>(
      () =>
        categoryIcon.reduce<Record<string, { icon: string; color: string }>>(
          (acc, { name, icon, color }) => {
            acc[name] = { icon, color };
            return acc;
          },
          {}
        )
    );

    const sortedTotalsPerReceipt = computed(() =>
      [...props.totalsPerReceipt].sort((a, b) =>
        receiptSortCriteria.value === "Datum"
          ? new Date(b.date).getTime() - new Date(a.date).getTime()
          : b.total - a.total
      )
    );

    const sortedTotalsPerCategory = computed(() =>
      Object.entries(props.totalsPerCategory)
        .map(([category, total]) => ({ category, total }))
        .sort((a, b) =>
          categorySortCriteria.value === "Name"
            ? a.category.localeCompare(b.category)
            : b.total - a.total
        )
    );

    const receiptFilterItems = computed(() =>
      sortedTotalsPerReceipt.value.map((receipt) => ({
        id: String(receipt.id),
        label: formatDate(receipt.date),
        amount: receipt.total,
      }))
    );

    const categoryFilterItems = computed(() =>
      sortedTotalsPerCategory.value.map((item) => ({
        value: item.category,
        label: item.category,
        icon: categoryMetaByName.value[item.category]?.icon ?? "help_outline",
        iconColor: categoryMetaByName.value[item.category]?.color ?? "primary",
        amount: item.total,
      }))
    );

    const activeReceiptCount = computed(() => selectedReceiptIds.value.length);
    const categoryFilterStatus = computed(() =>
      selectedCategory.value ? "aktiv" : "inaktiv"
    );

    watch(
      () => props.totalsPerReceipt,
      (newReceipts) => {
        const currentSet = new Set(selectedReceiptIds.value);
        const availableIds = newReceipts.map((receipt) => String(receipt.id));

        if (availableIds.length === 0) {
          selectedReceiptIds.value = [];
          return;
        }

        if (currentSet.size === 0) {
          selectedReceiptIds.value = availableIds;
          return;
        }

        selectedReceiptIds.value = availableIds.filter((id) => currentSet.has(id));
        if (selectedReceiptIds.value.length === 0) {
          selectedReceiptIds.value = availableIds;
        }
      },
      { immediate: true }
    );

    watch(selectedReceiptIds, (newVal) => {
      emit("update:selectedReceipts", newVal.map((id) => String(id)));
    });

    watch(selectedCategory, (newVal) => {
      emit("update:selectedCategory", newVal);
    });

    return {
      selectedReceiptIds,
      selectedCategory,
      receiptSortCriteria,
      categorySortCriteria,
      receiptFilterItems,
      categoryFilterItems,
      activeReceiptCount,
      categoryFilterStatus,
    };
  },
});
</script>

<style lang="scss" scoped>
.my-card {
  color: $dark;
  border-radius: 15px;
  border: 1px solid $primary;
  margin-bottom: 12px;
  height: fit-content;
}

.filter-panel {
  width: 360px;
}

.custom-list {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  margin-top: 10px;
  gap: 8px;
}

.q-item:focus {
  background-color: none;
  border-radius: 15px;
}

:deep(.q-expansion-item) {
  .q-item__label {
    font-weight: bold;
  }
  .q-item__label--caption {
    font-weight: normal;
  }
  .q-focus-helper {
    border-radius: 15px;
  }
}

.text-h6 {
  font-weight: bold;
}

.custom-label {
  .q-item__label {
    font-weight: bold;
  }
}

.q-pa-md {
  padding: 16px;
}
</style>
