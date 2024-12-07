<template>
  <q-td
    :props="props"
    style="text-decoration: underline; cursor: pointer; text-underline-offset: 4px;"
  >
    <!-- Anzeige des aktuellen Werts -->
    {{ currentValue }}

    <!-- Popup für die Bearbeitung -->
    <q-popup-edit v-model="currentValue" v-slot="scope" auto-save>
      <div v-if="fieldName === 'Category'">
        <q-select
          v-model="scope.value"
          :options="categories"
          dense
          autofocus
          @keyup.enter="
            () => {
              scope.set();
              updateCategory(props.row);
            }
          "
        />
      </div>

      <div v-else-if="fieldName === 'Unit'">
        <q-input
          v-model="scope.value"
          dense
          autofocus
          placeholder="Einheit in stk/kg/g/ml/cl/l eingeben"
          @keyup.enter="
            () => {
              scope.set();
              updateUnit(props.row);
            }
          "
        />
      </div>
    </q-popup-edit>
  </q-td>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from "vue";
import { Article } from "../../helpers/interfaces/article.interface";

/**
 * Diese Komponente übernimmt die Bearbeitung von Kategorie oder Einheit basierend auf dem fieldName-Prop.
 * fieldName: "Category" oder "Unit"
 * - Bei Category: Es wird ein q-select mit Kategorien angezeigt
 * - Bei Unit: Es wird ein q-input für die Einheit angezeigt
 */
export default defineComponent({
  name: "EditableCell",
  props: {
    props: {
      type: Object as PropType<{
        row: Article;
        col: any;
        rowIndex: number;
        colIndex: number;
      }>,
      required: true,
    },
    fieldName: {
      type: String as PropType<"Category" | "Unit">,
      required: true,
    },
    categories: {
      type: Array as PropType<string[]>,
      default: () => [],
    },
    updateCategory: {
      type: Function as PropType<(article: Article) => void>,
      default: () => {},
    },
    updateUnit: {
      type: Function as PropType<(article: Article) => void>,
      default: () => {},
    },
  },
  setup(props) {
    const currentValue = computed({
      get() {
        return props.props.row[props.fieldName];
      },
      set(newVal: string) {
        props.props.row[props.fieldName] = newVal;
      },
    });

    return {
      currentValue,
    };
  },
});
</script>
