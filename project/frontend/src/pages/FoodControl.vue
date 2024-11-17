<template>
  <div class="custom-container q-gutter-md">
    <q-card flat bordered class="first">
      <q-card-section>
        <q-item-label class="text-h6" style="font-size: 16px"
          >Gesamtausgaben</q-item-label
        >
        <q-item-label class="text-h6" caption style="min-height: 70px">
          <template v-if="totalExpenses.firstMonth">
          vom {{ formatMonth(totalExpenses.firstMonth) }} {{ totalExpenses.firstYear }}
          -
          {{ formatMonth(totalExpenses.lastMonth) }}
          {{ totalExpenses.lastYear }}</template></q-item-label
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
        <q-item-label class="text-h6" style="font-size: 16px"
          >Top Kategorie</q-item-label
        >

        <q-item-label class="text-h6" caption style="min-height: 70px"
          ><template v-if="topCategory.name">
            <q-icon size="1.9em" name="star" color="amber" />
            {{ topCategory.name }}</template
          ></q-item-label
        >
      </q-card-section>
      <q-card-actions align="center">
        <q-item class="text-h5">
          <div>{{ topCategory.total.toFixed(2) }} CHF</div>
        </q-item>
      </q-card-actions>
    </q-card>
    <q-card flat bordered class="third">
      <q-card-section>
        <q-item-label class="text-h6" style="font-size: 16px">
          Top 5 Artikel
        </q-item-label>
      </q-card-section>
      <q-card-section>
        <q-list>
          <q-item
            v-for="(item, index) in topFiveItems"
            :key="index"
            style="min-height: auto; padding: 0"
          >
            <q-item-section>
              <q-item-label style="font-size: 12px"
                >{{ item.Name.slice(0, 10) + ".." || "xxx"
                }}<q-tooltip anchor="center left" class="text-h5"
                  >{{ item.Name }},
                  {{ formatDate(item.Purchase_Date) }}</q-tooltip
                >
              </q-item-label>
            </q-item-section>
            <q-item-section side>
              <div>{{ item.Total.toFixed(2) }} CHF</div>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
    </q-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { TotalExpenses } from "../helpers/interfaces/totalExpenses.interface";
import { formatMonth, formatDate } from "../helpers/dateHelpers";

export default defineComponent({
  name: "FoodControl",

  props: {
    totalExpenses: {
      type: Object as PropType<TotalExpenses>,
      required: true,
    },
    rows: {
      type: Array as PropType<
        Array<{
          Name: string;
          Total: number;
          Purchase_Date: string;
          Category: string;
        }>
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
    topFiveItems() {
      return [...this.rows].sort((a, b) => b.Total - a.Total).slice(0, 5);
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
  max-height: 200px;
  border-color: transparent;
}

.first {
  background-color: $card-blue;
}

.second {
  background-color: $secondary;
}

.third {
  background-color: $amber;
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
