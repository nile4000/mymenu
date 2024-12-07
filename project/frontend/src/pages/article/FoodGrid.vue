<template>
  <div
    class="q-pa-xs col-xs-12 col-sm-6 col-md-4 col-lg-3 grid-style-transition"
  >
    <q-card
      bordered
      flat
      dense
      :class="selected ? ($q.dark.isActive ? '$positive' : 'bg-grey-2') : ''"
    >
      <q-card-section class="q-pa-xs row justify-between">
        <q-checkbox
          style="white-space: break-all; width: 80%"
          v-model="selected"
          checked-icon="radio_button_checked"
          unchecked-icon="radio_button_unchecked"
          :label="row.Name"
        />
        <q-icon
          :name="getCategoryIcon(row.Category)"
          :color="getCategoryColor(row.Category)"
          size="md"
        >
          <q-tooltip anchor="center left" class="text-h6">
            {{ row.Category }}
          </q-tooltip>
        </q-icon>
      </q-card-section>

      <q-separator />

      <q-list dense style="padding-bottom: 7px">
        <q-item
          v-for="col in cols.filter((col) => col.name !== 'desc')"
          :key="col.name"
        >
          <q-item-section>
            <q-item-label style="font-weight: bold">
              {{ col.label }}
            </q-item-label>
          </q-item-section>
          <q-item-section side>
            <q-item-label caption>{{ col.value }}</q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import { Article } from "../../helpers/interfaces/article.interface";
import { useQuasar } from "quasar";
import { categoryIcon } from "../../components/prompts/categorization";

interface Column {
  name: string;
  label: string;
  value: any;
}

export default defineComponent({
  name: "FoodGrid",
  props: {
    row: {
      type: Object as PropType<Article>,
      required: true,
    },
    cols: {
      type: Array as PropType<Column[]>,
      required: true,
    },
    selected: {
      type: Boolean,
      default: false,
    },
  },
  emits: ["update:selected"],
  setup() {
    const $q = useQuasar();

    const getCategoryIcon = (categoryName: string): string => {
      const cat = categoryIcon.find((c) => c.name === categoryName);
      return cat ? cat.icon : "help_outline";
    };

    const getCategoryColor = (categoryName: string): string => {
      const cat = categoryIcon.find((c) => c.name === categoryName);
      return cat ? cat.color : "primary";
    };

    return {
      $q,
      getCategoryIcon,
      getCategoryColor,
    };
  },
});
</script>

<style scoped>
.grid-style-transition {
  transition: all 0.3s ease;
}
</style>
