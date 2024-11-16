<template>
  <div class="custom-container q-gutter-md">
    <ScannerPage></ScannerPage>
    <q-card flat bordered class="first">
      <q-card-section>
        <q-item-label class="text-h6" style="font-size: 16px">Gesamtausgaben</q-item-label>
        <q-item-label class="text-h6" caption>
          vom {{ formatMonth(totalExpenses.firstMonth) }}
          -
          {{ formatMonth(totalExpenses.lastMonth) }}
          {{ totalExpenses.lastYear }}</q-item-label
        >
      </q-card-section>
      <q-card-actions align="center">
        <q-item class="text-h5">
          <div>{{ totalExpenses.sum.toFixed(2) }} CHF</div>
        </q-item>
      </q-card-actions>
    </q-card>
    <q-card flat bordered class="second">
      <q-card-section>
        <q-item-label class="text-h6" style="font-size: 16px">Top Kategorie</q-item-label>
        <template v-if="topCategory.name">
          <q-item-label class="text-h6" caption
            ><q-icon size="1.9em" name="star" color="amber" />
            {{ topCategory.name }}</q-item-label
          >
        </template>
      </q-card-section>
      <q-card-actions align="center">
        <q-item class="text-h5">
          <div>{{ topCategory.total.toFixed(2) }} CHF</div>
        </q-item>
      </q-card-actions>
    </q-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { TotalExpenses } from "../helpers/interfaces/totalExpenses.interface";
import { formatMonth, formatDate } from "../helpers/dateHelpers";
import ScannerPage from "./Scanner.vue";

export default defineComponent({
  name: "FoodDashboard",
  components: {
    ScannerPage,
  },
  props: {
    totalExpenses: {
      type: Object as PropType<TotalExpenses>,
      required: true,
    },
    rows: {
      type: Array as PropType<
        Array<{ Total: number; Purchase_Date: string; Category: string }>
      >,
      default: () => [],
    },
  },
  computed: {
    topCategory() {
      const categoryTotals: Record<string, number> = {};
      (this.rows || []).forEach((item) => {
        const category = item.Category;
        if (category) {
          categoryTotals[category] =
            (categoryTotals[category] || 0) + item.Total;
        }
      });
      const topCategory = Object.entries(categoryTotals).reduce(
        (max, [name, total]) => (total > max.total ? { name, total } : max),
        { name: "", total: 0 }
      );
      return topCategory;
    },
  },
  methods: {
    formatMonth,
    formatDate,
  },
});
</script>

<style lang="scss" scoped>
.custom-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}
.q-card {
  border-radius: 25px;
  width: 200px;
  min-height: 200px;
  border-color: transparent;
}

.first {
  background-color: $card-blue;
}

.second {
  background-color: $secondary;
}

.text-h5 {
  color: $dark;
  text-decoration: underline;
  text-decoration-color: white;
  text-decoration-thickness: 1.5px;
  transition: text-decoration-color 0.5s ease;
  text-underline-offset: 4px;
}

.text-h5:hover {
  text-decoration: underline;
  text-decoration-color: $dark;
}

.text-caption {
  font-size: 15px;
}

.text-h6 {
  font-weight: bold;
  word-break: break-all;
}

.q-pa-md {
  padding: 16px;
}
</style>
