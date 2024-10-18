<template>
  <div class="q-pa-md">
    <q-row gutter="16">
      <!-- Gesamtausgaben -->
      <q-col cols="12" sm="6" md="6" lg="4" xl="3">
        <q-expansion-item
          label="Gesamtausgaben"
          icon="monetization_on"
          expand-separator
          :dense="false"
          default-opened
          class="my-card shadow-2"
        >
          <template v-slot:default>
            <q-separator />
            <div
              v-if="
                totalExpenses.firstMonth &&
                totalExpenses.firstYear &&
                totalExpenses.lastMonth &&
                totalExpenses.lastYear
              "
            >
              <div class="text-subtitle2 text-grey flex flex-center">
                von {{ formatMonth(totalExpenses.firstMonth) }}
                {{ totalExpenses.firstYear }} -
                {{ formatMonth(totalExpenses.lastMonth) }}
                {{ totalExpenses.lastYear }}
              </div>
            </div>
            <div class="flex flex-center">
              <div class="text-h5 text-primary">
                {{ totalExpenses.sum.toFixed(2) }} CHF
              </div>
            </div>
          </template>
        </q-expansion-item>
      </q-col>
      <!-- Top-Kategorie -->
      <q-col cols="12" sm="6" md="6" lg="4" xl="3">
        <q-expansion-item
          label="Top Kategorie"
          icon="star"
          expand-separator
          :dense="false"
          class="my-card shadow-2"
          caption="Kategorie mit den höchsten Ausgaben"
        >
          <template v-slot:default>
            <q-separator />
            <q-list>
              <q-item>
                <q-item-section>
                  {{ topCategory.name }}
                </q-item-section>
                <q-item-section side>
                  {{ topCategory.total.toFixed(2) }} CHF
                </q-item-section>
              </q-item>
            </q-list>
          </template>
        </q-expansion-item>
      </q-col>
      <q-col cols="12" sm="6" md="6" lg="4" xl="3">
        <q-expansion-item
          label="Total pro Kategorie"
          icon="category"
          expand-separator
          :dense="false"
          class="my-card shadow-2"
        >
          <template v-slot:default>
            <q-separator />
            <q-list>
              <q-item
                v-for="(total, category) in totalsPerCategory"
                :key="category"
              >
                <q-item-section>
                  {{ category }}
                </q-item-section>
                <q-item-section side>
                  {{ total.toFixed(2) }} CHF
                </q-item-section>
              </q-item>
            </q-list>
          </template>
        </q-expansion-item>
      </q-col>

      <!-- Total pro Beleg -->
      <q-col cols="12" sm="6" md="6" lg="4" xl="3">
        <q-expansion-item
          label="Total pro Beleg"
          icon="receipt_long"
          expand-separator
          :dense="false"
          class="my-card shadow-2"
          caption="Total CHF (eingescannt)."
        >
          <template v-slot:default>
            <q-separator />
            <q-list>
              <q-item v-for="receipt in totalsPerReceipt" :key="receipt.id">
                <q-item-section>
                  {{ formatDate(receipt.date) }}
                </q-item-section>
                <q-item-section side>
                  <div class="text-body1">
                    {{ receipt.total.toFixed(2) }} CHF
                  </div>
                </q-item-section>
              </q-item>
            </q-list>
          </template>
        </q-expansion-item>
      </q-col>

      <!-- Total pro Beleg (Berechnet) -->
      <q-col cols="12" sm="6" md="6" lg="4" xl="3">
        <q-expansion-item
          label="Total pro Beleg (Berechnet)"
          icon="calculate"
          expand-separator
          :dense="false"
          class="my-card shadow-2"
          caption="Artikel-Totale zusammengerechnet. Ohne Bons."
        >
          <template v-slot:default>
            <q-separator />
            <q-list>
              <q-item
                v-for="(data, receiptId) in totalCalculatedPerReceipt"
                :key="receiptId"
              >
                <q-item-section>
                  {{ formatDate(data.date.toString()) }}
                </q-item-section>
                <q-item-section side>
                  <div class="text-body1">{{ data.total.toFixed(2) }} CHF</div>
                </q-item-section>
              </q-item>
            </q-list>
          </template>
        </q-expansion-item>
      </q-col>
    </q-row>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { TotalExpenses } from "../helpers/interfaces/totalExpenses.interface";
import { formatMonth, formatDate } from "../helpers/dateHelpers";

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
    rows: {
      type: Array as PropType<
        Array<{ Total: number; Purchase_Date: string; Category: string }>
      >,
      default: () => [],
    },
    totalCalculatedPerReceipt: {
      type: Object as PropType<Record<string, { total: number; date: string }>>,
      required: true,
    },
    totalExpenses: {
      type: Object as PropType<TotalExpenses>,
      required: true,
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

<style scoped>
.my-card {
  width: 100%;
  max-width: 400px;
  margin-top: 10px;
  margin-bottom: 10px;
}

.text-h6 {
  font-weight: bold;
}

.q-pa-md {
  padding: 16px;
}
</style>
