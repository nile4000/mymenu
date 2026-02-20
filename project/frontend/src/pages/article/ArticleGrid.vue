<template>
  <div
    class="q-pa-xs col-xs-12 col-sm-6 col-md-4 col-lg-3 grid-style-transition"
  >
    <q-card
      bordered
      flat
      dense
      class="clickable-card"
      :class="selected ? (q.dark.isActive ? '$positive' : 'bg-grey-2') : ''"
      @click="$emit('update:selected', !selected)"
    >
      <q-card-section class="q-pa-xs card-header">
        <q-icon
          class="category-icon"
          :name="getCategoryIcon(row.Category || '')"
          :color="getCategoryColor(row.Category || '')"
          size="md"
        >
          <q-tooltip anchor="center left" class="text-h6">
            {{ row.Category }}
          </q-tooltip>
        </q-icon>
        <div class="article-name">
          {{ row.Name }}
        </div>
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
import { QVueGlobals, useQuasar } from "quasar";
import { defineComponent, PropType } from "vue";
import { categoryIcon } from "../../components/prompts/categorization";
import { Article } from "../../helpers/interfaces/article.interface";

interface Column {
  name: string;
  label: string;
  value: any;
}

export default defineComponent({
  name: "ArticleGrid",
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
    const q: QVueGlobals = useQuasar();

    const getCategoryIcon = (categoryName: string): string => {
      const cat = categoryIcon.find((c) => c.name === categoryName);
      return cat ? cat.icon : "help_outline";
    };

    const getCategoryColor = (categoryName: string): string => {
      const cat = categoryIcon.find((c) => c.name === categoryName);
      return cat ? cat.color : "primary";
    };

    return {
      q,
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

.clickable-card {
  cursor: pointer;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  flex-shrink: 0;
  margin-left: 4px;
}

.article-name {
  margin: 5px 5px 5px 0;
  white-space: break-all;
}
</style>
