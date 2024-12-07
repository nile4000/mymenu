<template>
  <div class="custom-container">
    <q-card flat class="first custom-card">
      <q-card-section>
        <q-item-label class="text-h6" style="font-size: 16px"
          >Gesamtausgaben</q-item-label
        >
        <q-item-label class="text-h6" caption style="min-height: fit-content">
          <template v-if="totalExpenses.firstMonth">
            vom {{ formatMonth(totalExpenses.firstMonth) }}
            {{ totalExpenses.firstYear }}
            -
            {{ formatMonth(totalExpenses.lastMonth || 0) }}
            {{ totalExpenses.lastYear }}</template
          ></q-item-label
        >
      </q-card-section>
      <q-card-actions align="center">
        <q-item class="text-h5" style="font-weight: bold">
          <div>{{ totalExpenses.sum.toFixed(2) }} CHF</div>
        </q-item>
      </q-card-actions>
    </q-card>
    <q-card flat class="second custom-card">
      <q-card-section>
        <q-item-label class="text-h6" style="font-size: 16px"
          >Top Kategorie</q-item-label
        >

        <q-item-label class="text-h6" caption style="min-height: fit-content"
          ><template v-if="topCategory.name">
            <q-icon size="1.9em" name="star" color="amber" />
            {{ topCategory.name }}</template
          ></q-item-label
        >
      </q-card-section>
      <q-card-actions align="center">
        <q-item class="text-h5" style="font-weight: bold">
          <div>{{ topCategory.total.toFixed(2) }} CHF</div>
        </q-item>
      </q-card-actions>
    </q-card>
    <q-card flat class="third custom-card">
      <q-card-section>
        <q-item-label class="text-h6" style="font-size: 16px">
          Top 5 Artikel
        </q-item-label>
      </q-card-section>
      <q-card-section align="left">
        <q-list style="padding-bottom: 5px">
          <q-item
            v-for="(item, index) in topFiveItems"
            :key="index"
            style="min-height: auto; padding: 0"
          >
            <q-item-section>
              <q-item-label style="font-size: 14px"
                ><span class="label-desktop">{{
                  item.Name.slice(0, 12) + ".." || "xxx"
                }}</span
                ><span class="label-mobile">{{
                  item.Name.slice(0, 21) + ".." || "xxx"
                }}</span>
                <q-tooltip anchor="center left" class="text-h5"
                  >{{ item.Name }} |
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
import { TotalExpenses } from "../../helpers/interfaces/totalExpenses.interface";
import { formatMonth, formatDate } from "../../helpers/dateHelpers";

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
  justify-content: space-evenly;
  gap: 8px;
  padding-top: 8px;
}
.custom-card {
  border-radius: 25px;
  width: 240px;
  margin-bottom: 12px;
  max-height: 173px;
}
.label-desktop {
  display: block;
}
.label-mobile {
  display: none;
}
@media (max-width: 700px) {
  .custom-container {
    display: flex;
    flex-direction: column;
  }
  .label-desktop {
    display: none;
  }
  .label-mobile {
    display: block;
  }
  .custom-container {
    flex-wrap: wrap;
  }
  .custom-card {
    width: 340px;
    height: auto;
  }
}

.q-card__section {
  padding-bottom: 0;
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

.text-caption {
  font-size: 15px;
}

.text-h5 {
  color: $dark;
}

.text-h6 {
  font-weight: bold;
  word-break: break-word;
}

.q-pa-md {
  padding: 16px;
}
</style>
