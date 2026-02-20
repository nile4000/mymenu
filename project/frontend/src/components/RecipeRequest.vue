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
          <h5 style="margin-block-end: 20px">Einstellungen</h5>
          <q-badge color="secondary" class="q-mb-md">
            Anzahl Personen: {{ standard }} (0-10)
          </q-badge>
          <q-slider
            v-model="standard"
            :min="1"
            :max="10"
            style="max-width: 300px"
            color="secondary"
          />
        </q-card-section>
        <q-card-section style="margin-top: 0px">
          <q-expansion-item :dense="false" class="custom-card">
            <template v-slot:header>
              <q-item-section avatar>
                <q-avatar icon="category" class="colored-icon" />
              </q-item-section>
              <q-item-section style="font-weight: bold"
                >Kategorie</q-item-section
              >
            </template>
            <q-list>
              <q-item v-for="item in categories" :key="item">
                <q-item-section>
                  {{ item }}
                </q-item-section>
              </q-item>
            </q-list>
          </q-expansion-item>
        </q-card-section>
        <q-card-section style="margin-top: 0px">
          <q-expansion-item :dense="false" class="custom-card">
            <template v-slot:header>
              <q-item-section avatar>
                <q-avatar icon="receipt_long" class="colored-icon2" />
              </q-item-section>
              <q-item-section style="font-weight: bold"
                >Kassenzettel</q-item-section
              >
            </template>
            <q-list>
              <q-item v-for="item in categories" :key="item">
                <q-item-section>
                  {{ item }}
                </q-item-section>
              </q-item>
            </q-list>
          </q-expansion-item>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Abbrechen" color="negative" v-close-popup />
          <q-btn
            flat
            label="Erstellen"
            color="primary"
            style="font-weight: bold"
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
import { defineComponent, PropType, ref } from "vue";
import { QVueGlobals, useQuasar } from "quasar";
import { handleError } from "../helpers/composables/useErrors";
import { categories } from "./prompts/recipe";
import { sendSingleRecipeRequest } from "../services/recipeRequest";
import { Article } from "../helpers/interfaces/article.interface";

export default defineComponent({
  name: "RecipeRequest",
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  emits: ["addRecipe"],
  setup(props, { emit }) {
    const $q: QVueGlobals = useQuasar();
    const isLoading = ref(false);
    const showDialogRecipe = ref(false);
    const standard = ref(2);

    const handleSendRequest = async () => {
      try {
        if (props.selectedItems.length === 0) {
          $q.notify({ type: "warning", message: "Keine Artikel ausgewählt!" });
          return;
        }

        isLoading.value = true;
        $q.loading.show({ message: "Rezept wird erstellt..." });

        const lastItems = props.selectedItems.slice(-30);
        const recipe = await sendSingleRecipeRequest(lastItems, standard.value);

        emit("addRecipe", recipe);

        $q.notify({
          type: "positive",
          message: "Rezept erfolgreich erstellt!",
        });
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
      categories,
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
