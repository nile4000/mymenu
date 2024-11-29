<template>
  <q-list class="justify-center custom-list">
    <!-- Total pro Beleg -->
    <q-expansion-item
      :dense="false"
      class="my-card"
      caption="Total CHF (eingescannt)."
    >
      <template v-slot:header>
        <q-item-section avatar>
          <q-avatar icon="receipt_long" class="colored-icon2" />
        </q-item-section>
        <q-item-section style="font-weight: bold">
          Total pro Beleg
        </q-item-section>
      </template>

      <!-- Sortieroption für Receipts -->
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

      <q-scroll-area style="height: 200px">
        <q-list>
          <q-item v-for="receipt in sortedTotalsPerReceipt" :key="receipt.id">
            <q-item-section>
              {{ formatDate(receipt.date) }}
            </q-item-section>
            <q-item-section>
              <div class="text-body1">{{ receipt.total.toFixed(2) }}.-</div>
            </q-item-section>
            <q-item-section side>
              <q-icon
                :name="
                  selectedReceipts[receipt.id] ? 'visibility' : 'visibility_off'
                "
                @click="
                  toggleReceiptSelection(
                    receipt.id,
                    !selectedReceipts[receipt.id]
                  )
                "
                class="cursor-pointer"
              >
                <q-tooltip anchor="center left" class="text-h6">
                  Artikel Ein-/Ausblenden
                </q-tooltip>
              </q-icon>
            </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-expansion-item>

    <!-- Total pro Kategorie -->
    <q-expansion-item default-opened color="primary" :dense="false" class="my-card">
      <template v-slot:header>
        <q-item-section avatar>
          <q-avatar icon="hub" class="colored-icon" />
        </q-item-section>
        <q-item-section style="font-weight: bold">
          Total pro Kategorie
        </q-item-section>
      </template>

      <!-- Sortieroption für Kategorien -->
      <div class="q-pa-sm">
        <q-select
          v-model="categorySortCriteria"
          :options="[
            { label: 'Name', value: 'Name' },
            { label: 'Total', value: '^Total' },
          ]"
          label="Sortieren nach"
          dense
          outlined
          rounded
        />
      </div>

      <q-scroll-area style="height: 300px">
        <q-list>
          <q-item v-for="item in sortedTotalsPerCategory" :key="item.category">
            <q-item-section>
              <q-icon
                size="sm"
                :name="getCategoryIcon(item.category)"
                :color="getCategoryColor(item.category)"
              />
            </q-item-section>
            <q-item-section>
              {{ item.category }}
            </q-item-section>
            <q-item-section side>
              <div class="text-body1">{{ item.total.toFixed(2) }}.-</div>
            </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-expansion-item>
  </q-list>
</template>

<script lang="ts">
import { defineComponent, PropType, reactive, watch, computed, ref } from "vue";
import { formatDate } from "../../helpers/dateHelpers";
import { categoryIcon } from "../../components/prompts/categorization";

export default defineComponent({
  name: "FoodTotal",
  props: {
    totalsPerCategory: {
      type: Object as () => Record<string, number>,
      required: true,
    },
    totalsPerReceipt: {
      type: Array as PropType<
        Array<{ id: string; date: string; total: number }>
      >,
      required: true,
    },
    totalCalculatedPerReceipt: {
      type: Object as PropType<Record<string, { total: number; date: string }>>,
      required: true,
    },
  },
  emits: ["update:selectedReceipts"],
  setup(props, { emit }) {
    const selectedReceipts = reactive<Record<string, boolean>>({});

    const receiptSortCriteria = ref<"Datum" | "Total">("Datum");
    const categorySortCriteria = ref<"Name" | "Total">("Name");

    const sortedTotalsPerReceipt = computed(() => {
      return [...props.totalsPerReceipt].sort((a, b) => {
        if (receiptSortCriteria.value === "Datum") {
          return new Date(b.date).getTime() - new Date(a.date).getTime();
        } else {
          return b.total - a.total;
        }
      });
    });
    const sortedTotalsPerCategory = computed(() => {
      const categoriesArray = Object.entries(props.totalsPerCategory).map(
        ([category, total]) => ({
          category,
          total,
        })
      );
      return categoriesArray.sort((a, b) => {
        if (categorySortCriteria.value === "Name") {
          return a.category.localeCompare(b.category);
        } else {
          return b.total - a.total;
        }
      });
    });

    const toggleReceiptSelection = (id: string, value: boolean) => {
      selectedReceipts[id] = value;
    };

    const getCategoryIcon = (categoryName: string): string => {
      const category = categoryIcon.find((c) => c.name === categoryName);
      return category ? category.icon : "help_outline"; // "help_outline" als Standard-Icon
    };

    const getCategoryColor = (categoryName: string): string => {
      const category = categoryIcon.find((c) => c.name === categoryName);
      return category ? category.color : "primary";
    };

    watch(
      () => props.totalsPerReceipt,
      (newReceipts) => {
        if (newReceipts && newReceipts.length > 0) {
          newReceipts.forEach((receipt) => {
            if (selectedReceipts[receipt.id] === undefined) {
              selectedReceipts[receipt.id] = true;
            }
          });
        }
      },
      { immediate: true, deep: true }
    );

    watch(
      () => ({ ...selectedReceipts }),
      (newVal) => {
        const selectedIds = Object.keys(newVal).filter((id) => newVal[id]);
        emit("update:selectedReceipts", selectedIds);
      },
      { immediate: true, deep: true }
    );

    return {
      selectedReceipts,
      toggleReceiptSelection,
      formatDate,
      sortedTotalsPerReceipt,
      sortedTotalsPerCategory,
      receiptSortCriteria,
      categorySortCriteria,
      getCategoryIcon,
      getCategoryColor,
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
  max-width: fit-content;
  height: fit-content;
}

.q-item__section--main {
  flex: auto 1 !important;
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
