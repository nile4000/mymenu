<template>
  <div class="q-pa-md row items-start q-gutter-md">
    <!-- Gesamtausgaben Card -->
    <q-card class="my-card shadow-2">
      <q-card-section>
        <div class="text-h6">Gesamtausgaben</div>
      </q-card-section>
      <q-separator />
      <q-card-section class="flex flex-center">
        <div class="text-h5 text-primary">
          {{ totalExpenses.toFixed(2) }} CHF
        </div>
      </q-card-section>
    </q-card>

    <q-card class="my-card shadow-2">
      <q-card-section>
        <div class="text-h6">Total Berechnet (ohne Bon/Gesamtrabatte)</div>
      </q-card-section>
      <q-separator />
      <q-card-section class="flex flex-center">
        <div class="text-h5 text-primary">
          {{ totalCalculated.toFixed(2) }} CHF
        </div>
      </q-card-section>
    </q-card>

    <!-- Top-Kategorie nach Umsatz Card -->
    <q-card class="my-card shadow-2">
      <q-card-section>
        <div class="text-h6">Top-Kategorie</div>
        <div class="text-subtitle2 text-grey">
          Kategorie mit den höchsten Ausgaben
        </div>
      </q-card-section>
      <q-separator />
      <q-card-section class="flex flex-center">
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
      </q-card-section>
    </q-card>

    <!-- Total pro Kategorie Card -->
    <q-card class="my-card-wide shadow-2">
      <q-card-section>
        <div class="text-h6">Total pro Kategorie</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-list>
          <q-item
            v-for="(total, category) in totalsPerCategory"
            :key="category"
          >
            <q-item-section>
              {{ category }}
            </q-item-section>
            <q-item-section side>{{ total.toFixed(2) }} CHF </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
    </q-card>

    <!-- Total pro Kaufdatum Card -->
    <q-card class="my-card shadow-2">
      <q-card-section>
        <div class="text-h6">Total pro Beleg</div>
      </q-card-section>
      <q-separator />
      <q-card-section>
        <q-list>
          <q-item v-for="receipt in totalsPerReceipt" :key="receipt.id"
            ><q-item-section>
              {{ formatDate(receipt.date) }}
            </q-item-section>
            <q-item-section side>
              <div class="text-body1">{{ receipt.total.toFixed(2) }} CHF</div>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
    </q-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "FoodTotal",
  props: {
    totalsPerCategory: {
      type: Object as PropType<Record<string, number>>,
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
    totalCalculated: {
      type: Number,
      required: true,
    },
    totalExpenses: {
      type: Number,
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
    formatDate(dateString: string) {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat("de-DE", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
      }).format(date);
    },
  },
});
</script>

<style scoped>
.my-card {
  width: 100%;
  max-width: 300px;
}
.my-card-wide {
  width: 100%;
  max-width: 400px;
}
</style>
