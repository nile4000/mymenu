<template>
  <q-td
    :props="props"
    style="text-decoration: underline; cursor: pointer; text-underline-offset: 4px;"
  >
    <!-- Display value -->
    <template v-if="fieldName === 'Total'">{{ currentTotal.toFixed(2) }}</template>
    <template v-else>{{ currentText }}</template>

    <!-- Popup: Category -->
    <q-popup-edit
      v-if="fieldName === 'Category'"
      v-model="currentText"
      v-slot="scope"
      auto-save
    >
      <q-select
        v-model="scope.value"
        :options="categories"
        dense
        autofocus
        @update:model-value="(val) => onCategoryPicked(val, scope)"
      />
    </q-popup-edit>

    <!-- Popup: Unit -->
    <q-popup-edit
      v-else-if="fieldName === 'Unit'"
      v-model="currentText"
      v-slot="scope"
      auto-save
    >
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
        @blur="
          () => {
            scope.set();
            updateUnit(props.row);
          }
        "
      />
    </q-popup-edit>

    <!-- Popup: Total -->
    <q-popup-edit
      v-else-if="fieldName === 'Total'"
      v-model="currentTotal"
      v-slot="scope"
      auto-save
    >
      <q-input
        v-model.number="scope.value"
        type="number"
        step="0.01"
        dense
        autofocus
        suffix="CHF"
        @keyup.enter="
          () => {
            scope.set();
            updateTotal(props.row);
          }
        "
        @blur="
          () => {
            scope.set();
            updateTotal(props.row);
          }
        "
      />
    </q-popup-edit>
  </q-td>
</template>

<script lang="ts">
import { defineComponent, PropType, computed } from "vue";
import { useQuasar } from "quasar";
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
      type: String as PropType<"Category" | "Unit" | "Total">,
      required: true,
    },
    categories: {
      type: Array as PropType<string[]>,
      default: () => [],
    },
    updateCategory: {
      type: Function as PropType<(article: Article) => void>,
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      default: () => {},
    },
    updateUnit: {
      type: Function as PropType<(article: Article) => void>,
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      default: () => {},
    },
    updateTotal: {
      type: Function as PropType<(article: Article) => void>,
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      default: () => {},
    },
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
    bulkUpdateCategory: {
      type: Function as PropType<(articles: Article[], category: string) => void>,
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      default: () => {},
    },
  },
  setup(props) {
    const $q = useQuasar();

    const currentText = computed<string>({
      get(): string {
        if (props.fieldName === "Total") return "";
        const val = props.props.row[props.fieldName];
        return val === undefined || val === null ? "" : (val as string);
      },
      set(newVal: string) {
        if (props.fieldName === "Total") return;
        // eslint-disable-next-line vue/no-mutating-props
        (props.props.row as unknown as Record<string, string>)[props.fieldName] = newVal;
      },
    });

    const currentTotal = computed<number>({
      get(): number {
        const val = props.props.row.Total;
        return typeof val === "number" ? val : Number(val ?? 0);
      },
      set(newVal: number) {
        // eslint-disable-next-line vue/no-mutating-props
        props.props.row.Total = typeof newVal === "number" ? newVal : Number(newVal);
      },
    });

    function onCategoryPicked(val: string, scope: { value: string; set: () => void }) {
      const isCurrentSelected = props.selectedItems.some((a) => a.Id === props.props.row.Id);
      const others = props.selectedItems.filter((a) => a.Id !== props.props.row.Id);

      scope.value = val;
      scope.set();

      if (isCurrentSelected && others.length > 0) {
        $q.dialog({
          title: "Kategorie setzen",
          message: `Kategorie für alle ${props.selectedItems.length} selektierten Artikel auf "${val}" setzen?`,
          ok: { label: "Alle ändern", color: "primary", unelevated: true },
          cancel: { label: "Nur diesen", flat: true },
        }).onOk(() => {
          props.selectedItems.forEach((a) => {
            // eslint-disable-next-line vue/no-mutating-props
            a.Category = val;
          });
          props.bulkUpdateCategory(props.selectedItems, val);
        }).onCancel(() => {
          props.updateCategory(props.props.row);
        });
      } else {
        props.updateCategory(props.props.row);
      }
    }

    return {
      currentText,
      currentTotal,
      onCategoryPicked,
    };
  },
});
</script>
