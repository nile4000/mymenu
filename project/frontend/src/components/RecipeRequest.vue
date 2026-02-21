<template>
  <div>
    <q-dialog
      v-model="showDialogRecipe"
      persistent
      transition-show="slide-up"
      transition-hide="slide-down"
    >
      <q-card class="custom-card">
        <q-card-section class="column items-center">
          <h5 style="margin-block-end: 16px">Einstellungen</h5>

          <q-badge color="secondary" class="q-mb-sm">
            Anzahl Personen: {{ standard }} (1-10)
          </q-badge>
          <q-slider
            v-model="standard"
            :min="1"
            :max="10"
            style="max-width: 300px"
            color="secondary"
          />

          <q-badge color="primary" class="q-mt-sm">
            Auswahl: {{ filteredItems.length }} von {{ selectedItemsCount }} Artikeln
          </q-badge>
        </q-card-section>

        <q-card-section style="margin-top: 0px">
          <FilterPanel
            panel-class="custom-card"
            icon-class="colored-icon"
            icon="category"
            default-opened
            title="Filter: Kategorie"
          >
            <CategoryFilterList
              v-model="categoryModel"
              :options="categoryFilterItems"
              show-all-option
            />
          </FilterPanel>
        </q-card-section>

        <q-card-section style="margin-top: 0px">
          <FilterPanel
            panel-class="custom-card"
            icon-class="colored-icon2"
            icon="receipt_long"
            default-opened
            title="Filter: Kassenzettel"
          >
            <ReceiptFilterList
              v-model="safeGlobalSelectedReceiptIds"
              :options="availableReceipts"
              show-bulk-actions
            />
          </FilterPanel>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Abbrechen" color="negative" v-close-popup />
          <q-btn
            flat
            label="Erstellen"
            color="primary"
            style="font-weight: bold"
            :disable="isLoading"
            @click="handleSendRequest"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>

    <q-btn-group rounded>
      <q-btn
        unelevated
        rounded
        :disabled="isLoading"
        @click="showDialogRecipe = true"
        v-ripple
      >
        <q-icon size="1.9em" name="hub" color="secondary" />
        <div class="text-h6">Rezept</div>
      </q-btn>
    </q-btn-group>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, PropType, ref } from "vue";
import { QVueGlobals, useQuasar } from "quasar";
import { storeToRefs } from "pinia";
import FilterPanel from "./filters/FilterPanel.vue";
import CategoryFilterList from "./filters/CategoryFilterList.vue";
import ReceiptFilterList from "./filters/ReceiptFilterList.vue";
import { handleError } from "../helpers/composables/useErrors";
import { createRecipe } from "../services";
import { Article } from "../helpers/interfaces/article.interface";
import { Recipe } from "../helpers/interfaces/recipe.interface";
import { useFilterStore } from "../stores/filterStore";

type ReceiptOption = {
  id: string;
  label: string;
};

export default defineComponent({
  name: "RecipeRequest",
  components: {
    FilterPanel,
    CategoryFilterList,
    ReceiptFilterList,
  },
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  emits: ["addRecipe"],
  setup(props, { emit }) {
    const $q: QVueGlobals = useQuasar();
    const filterStore = useFilterStore();
    const {
      selectedCategory: globalSelectedCategory,
      selectedReceiptIds: globalSelectedReceiptIds,
    } = storeToRefs(filterStore);

    const isLoading = ref(false);
    const showDialogRecipe = ref(false);
    const standard = ref(2);

    const categoryModel = computed<string | null>({
      get: () => globalSelectedCategory.value,
      set: (value) => {
        globalSelectedCategory.value = value;
      },
    });

    const safeGlobalSelectedReceiptIds = computed<string[]>({
      get: () =>
        Array.isArray(globalSelectedReceiptIds.value)
          ? globalSelectedReceiptIds.value.map((id) => String(id))
          : [],
      set: (value) => {
        globalSelectedReceiptIds.value = Array.isArray(value)
          ? value.map((id) => String(id))
          : [];
      },
    });

    const categoryFilterItems = computed(() => {
      const categories = Array.from(
        new Set(
          props.selectedItems
            .map((item) => item.Category)
            .filter((category): category is string => Boolean(category))
        )
      ).sort((a, b) => a.localeCompare(b));

      const activeCategory = categoryModel.value;
      if (activeCategory && !categories.includes(activeCategory)) {
        categories.unshift(activeCategory);
      }

      return categories.map((item) => ({
        value: item,
        label: item,
      }));
    });

    const availableReceipts = computed<ReceiptOption[]>(() => {
      const map = new Map<string, string>();

      props.selectedItems.forEach((item) => {
        if (!item.Receipt_Id) {
          return;
        }

        const receiptId = String(item.Receipt_Id);
        const fallbackDate = item.Purchase_Date || "Unbekanntes Datum";
        const label = `${fallbackDate} (${receiptId.slice(0, 8)})`;
        if (!map.has(receiptId)) {
          map.set(receiptId, label);
        }
      });

      return Array.from(map.entries())
        .map(([id, label]) => ({ id, label }))
        .sort((a, b) => b.label.localeCompare(a.label));
    });

    const filteredItems = computed<Article[]>(() => {
      const activeReceiptIds = safeGlobalSelectedReceiptIds.value;

      return props.selectedItems.filter((item) => {
        const matchesCategory =
          !categoryModel.value || item.Category === categoryModel.value;

        const matchesReceipt =
          activeReceiptIds.length === 0 ||
          (item.Receipt_Id
            ? activeReceiptIds.includes(String(item.Receipt_Id))
            : false);

        return matchesCategory && matchesReceipt;
      });
    });

    const selectedItemsCount = computed(() =>
      Array.isArray(props.selectedItems) ? props.selectedItems.length : 0
    );

    const handleSendRequest = async () => {
      try {
        if (filteredItems.value.length === 0) {
          $q.notify({
            type: "warning",
            message: "Keine passenden Artikel ausgewaehlt!",
          });
          return;
        }

        isLoading.value = true;
        $q.loading.show({ message: "Rezept wird erstellt..." });

        const recipeResult = await createRecipe(
          filteredItems.value.slice(-30),
          standard.value
        );
        if (!recipeResult.ok) {
          handleError("Rezept erstellen", recipeResult.error.message, $q);
          return;
        }

        emit("addRecipe", recipeResult.data as Recipe);
        $q.notify({ type: "positive", message: "Rezept erfolgreich erstellt!" });
      } catch (error) {
        handleError("Rezept erstellen", error, $q);
      } finally {
        isLoading.value = false;
        $q.loading.hide();
        showDialogRecipe.value = false;
      }
    };

    return {
      isLoading,
      showDialogRecipe,
      standard,
      categoryModel,
      safeGlobalSelectedReceiptIds,
      categoryFilterItems,
      availableReceipts,
      filteredItems,
      selectedItemsCount,
      handleSendRequest,
    };
  },
});
</script>

<style lang="scss" scoped>
.custom-card {
  background-color: $bar-background;
  border-radius: 15px;
  border: 1px solid $dark;

  h5 {
    margin-block-start: 0px;
  }

  :deep(.q-focus-helper) {
    display: none;
  }
}

.colored-icon {
  :deep(.q-avatar__content) {
    .q-icon {
      color: $secondary !important;
    }
  }
}

.colored-icon2 {
  :deep(.q-avatar__content) {
    .q-icon {
      color: $negative !important;
    }
  }
}

.q-btn {
  height: 45px;
}

.q-icon {
  margin-right: 10px;
}

.text-h6 {
  text-transform: none;
}

.q-btn-group {
  position: fixed;
  bottom: 0;
  right: 25px;
  margin-bottom: 10px;
  z-index: 1000;
  background-color: $bar-background;
  border: 1px solid $primary;
}
</style>
