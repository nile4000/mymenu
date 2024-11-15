<template>
  <q-list style="margin-left: 20px">
    <q-expansion-item
      label="Total pro Kategorie"
      icon="category"
      expand-separator
      expand-icon="0"
      :dense="false"
      class="my-card shadow-2"
    >
      <q-list>
        <q-item v-for="(total, category) in totalsPerCategory" :key="category">
          <q-item-section>
            {{ category }}
          </q-item-section>
          <q-item-section side> {{ total.toFixed(2) }} CHF </q-item-section>
        </q-item>
      </q-list>
    </q-expansion-item>
    <q-expansion-item
      label="Total pro Beleg"
      icon="receipt_long"
      expand-separator
      expand-icon="0"
      :dense="false"
      default-opened
      class="my-card shadow-2"
      caption="Total CHF (eingescannt)."
    >
      <q-list>
        <q-item v-for="receipt in totalsPerReceipt" :key="receipt.id">
          <q-item-section>
            {{ formatDate(receipt.date) }}
          </q-item-section>
          <q-item-section side>
            <div class="text-body1">{{ receipt.total.toFixed(2) }} CHF</div>
          </q-item-section>
        </q-item>
      </q-list>
    </q-expansion-item>
  </q-list>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
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
    totalCalculatedPerReceipt: {
      type: Object as PropType<Record<string, { total: number; date: string }>>,
      required: true,
    },
  },
  methods: {
    formatMonth,
    formatDate,
  },
});
</script>

<style lang="scss" scoped>
.my-card {
  margin-bottom: 10px;
  border-radius: 15px;
}
.q-item:focus {
  background-color: none;
  border-radius: 15px;
}

:deep(.q-expansion-item) {
  .q-focus-helper {
    border-radius: 15px;
  }
}

.text-h6 {
  font-weight: bold;
}

.q-pa-md {
  padding: 16px;
}
</style>
