<template>
  <q-list class="justify-center custom-list">
    <!-- Total per receipt -->
    <q-expansion-item
      :dense="false"
      class="my-card filter-panel"
      caption="Klickbar: blendet Artikel pro Kassenzettel ein/aus."
    >
      <template v-slot:header>
        <q-item-section avatar>
          <q-avatar icon="receipt_long" class="colored-icon2" />
        </q-item-section>
        <q-item-section style="font-weight: bold">
          Filter: Kassenzettel
        </q-item-section>
        <q-item-section side>
          <q-badge color="primary" text-color="white">
            {{ activeReceiptCount }} aktiv
          </q-badge>
        </q-item-section>
      </template>

      <!-- Sortoptions for receipts -->
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

      <q-scroll-area class="filter-scroll">
        <q-list>
          <q-item
            v-for="receipt in sortedTotalsPerReceipt"
            :key="receipt.id"
            clickable
            :class="{ 'active-filter-item': selectedReceipts[receipt.id] }"
            @click="
              toggleReceiptSelection(receipt.id, !selectedReceipts[receipt.id])
            "
          >
            <q-item-section>
              {{ formatDate(receipt.date) }}
            </q-item-section>
            <q-item-section>
              <div class="text-body1">{{ receipt.total.toFixed(2) }} CHF</div>
            </q-item-section>
            <q-item-section side>
              <q-icon
                :name="
                  selectedReceipts[receipt.id] ? 'visibility' : 'visibility_off'
                "
                @click.stop="
                  toggleReceiptSelection(
                    receipt.id,
                    !selectedReceipts[receipt.id]
                  )
                "
                class="cursor-pointer"
              >
                <q-tooltip anchor="center left" class="text-h6">
                  Artikel pro Kassenzettel ein-/ausblenden
                </q-tooltip>
              </q-icon>
            </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-expansion-item>

    <!-- Total per category -->
    <q-expansion-item
      default-opened
      color="primary"
      :dense="false"
      class="my-card filter-panel"
      caption="Klickbar: filtert die Artikelliste nach Kategorie."
    >
      <template v-slot:header>
        <q-item-section avatar>
          <q-avatar icon="hub" class="colored-icon" />
        </q-item-section>
        <q-item-section style="font-weight: bold">
          Filter: Kategorien
        </q-item-section>
        <q-item-section side>
          <q-badge color="primary" text-color="white">
            {{ categoryFilterStatus }}
          </q-badge>
        </q-item-section>
      </template>

      <!-- Sortoption per category -->
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

      <q-scroll-area class="filter-scroll">
        <q-list>
          <q-item
            v-for="item in sortedTotalsPerCategory"
            :key="item.category"
            clickable
            :class="{
              'active-filter-item': selectedCategory === item.category,
            }"
            @click="toggleCategorySelection(item.category)"
          >
            <q-item-section>
              <q-icon
                size="sm"
                :name="getCategoryIcon(item.category)"
                :color="getCategoryColor(item.category)"
              />
            </q-item-section>
            <q-item-section style="margin-left: 18px">
              <div :style="{ 'font-weight': selectedCategory === item.category ? 'bold' : 'normal' }">
                {{ item.category }}
              </div>
            </q-item-section>
            <q-item-section side>
              <div class="text-body1">{{ item.total.toFixed(2) }} CHF</div>
            </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-expansion-item>
  </q-list>
</template>

<script lang="ts">
import { computed, defineComponent, PropType, reactive, ref, watch } from "vue";
import { categoryIcon } from "../../components/prompts/categorization";
import { formatDate } from "../../helpers/dateHelpers";

export default defineComponent({
  name: "ArticleTotal",
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
  },
  emits: ["update:selectedReceipts", "update:selectedCategory"],
  setup(props, { emit }) {
    const selectedReceipts = reactive<Record<string, boolean>>({});
    const receiptSortCriteria = ref<"Datum" | "Total">("Datum");
    const categorySortCriteria = ref<"Name" | "Total">("Name");
    const selectedCategory = ref<string | null>(null);
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

    const activeReceiptCount = computed(() =>
      sortedTotalsPerReceipt.value.filter(
        (receipt) => selectedReceipts[receipt.id]
      ).length
    );
    const categoryFilterStatus = computed(() =>
      selectedCategory.value ? "aktiv" : "inaktiv"
    );

    const toggleReceiptSelection = (id: string, value: boolean) => {
      selectedReceipts[id] = value;
    };

    const toggleCategorySelection = (category: string) => {
      // Reclicking on category removes the selected category
      if (selectedCategory.value === category) {
        selectedCategory.value = null;
      } else {
        selectedCategory.value = category;
      }
    };

    const getCategoryIcon = (categoryName: string): string => {
      return categoryMetaByName.value[categoryName]?.icon ?? "help_outline";
    };

    const getCategoryColor = (categoryName: string): string => {
      return categoryMetaByName.value[categoryName]?.color ?? "primary";
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

    watch(selectedCategory, (newVal) => {
      emit("update:selectedCategory", newVal);
    });

    return {
      selectedReceipts,
      toggleReceiptSelection,
      toggleCategorySelection,
      formatDate,
      sortedTotalsPerReceipt,
      sortedTotalsPerCategory,
      activeReceiptCount,
      categoryFilterStatus,
      receiptSortCriteria,
      categorySortCriteria,
      getCategoryIcon,
      getCategoryColor,
      selectedCategory,
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

.filter-scroll {
  height: 240px;
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

.active-filter-item {
  background-color: rgba(34, 46, 87, 0.08);
  border-left: 3px solid $primary;
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
