<template>
  <div>
    <div class="q-pa-md row justify-center">
      <RecipeRequest :selectedItems="recipeInputArticles" @addRecipe="handleAddRecipe" />
      <h5>Rezepte</h5>
    </div>
    <div class="row justify-center q-mb-sm">
      <q-badge color="primary" text-color="white">
        Zutatenbasis: {{ recipeInputArticles.length }} Artikel
      </q-badge>
    </div>
    <div class="row justify-center">
      <q-card
        v-for="recipe in recipes"
        :key="recipe.id"
        flat
        bordered
        class="q-ma-sm cards"
        :class="recipe.color"
      >
        <q-card-section class="row justify-center">
          <div class="text-h6" style="padding-bottom: 8px">
            {{ recipe.title }}
          </div>
          <div class="text-subtitle2">
            {{ recipe.description }}
          </div>
          <div>
            <q-icon name="schedule" /> {{ recipe.cookingTime }} |
            <q-icon name="category" /> {{ recipe.category }} |
            <q-icon name="groups" /> {{ recipe.servings }}
          </div>
        </q-card-section>
        <q-card-actions align="center" class="q-px-md">
          <q-btn disabled flat round icon="hub" class="btn-background">
            <q-tooltip anchor="center left" class="text-h6">Aendern</q-tooltip>
          </q-btn>
          <q-btn
            flat
            round
            color="primary"
            icon="north_east"
            class="btn-background"
            @click="goToRecipe(recipe)"
          >
            <q-tooltip anchor="center left" class="text-h6">Details</q-tooltip>
          </q-btn>
        </q-card-actions>
      </q-card>
      <q-banner
        v-if="recipes.length === 0"
        class="q-mt-md empty-state"
        rounded
      >
        Noch keine Rezepte vorhanden. Erstelle dein erstes Rezept mit dem Button.
      </q-banner>
      <q-dialog
        v-model="showDialog"
        persistent
        transition-show="slide-up"
        transition-hide="slide-down"
        @hide="closeOverlay"
      >
        <RecipeDetail
          v-if="selectedRecipe"
          :recipe="selectedRecipe"
          @close="closeOverlay"
        />
      </q-dialog>
    </div>
  </div>
</template>

<script lang="ts">
import { QVueGlobals, useQuasar } from "quasar";
import { storeToRefs } from "pinia";
import { computed, defineComponent, onMounted, ref, watch } from "vue";
import { onBeforeRouteLeave } from "vue-router";
import RecipeRequest from "../../components/RecipeRequest.vue";
import { handleError } from "../../helpers/composables/useErrors";
import { Recipe, RecipeIngredient } from "../../helpers/interfaces/recipe.interface";
import { useDataStore } from "../../stores/dataStore";
import RecipeDetail from "./RecipeDetail.vue";

const STORAGE_KEY = "mymenu_generated_recipes";
const CARD_COLORS = ["card-background1", "card-background2", "card-background3"] as const;

function selectCardColor(index: number): string {
  return CARD_COLORS[index % CARD_COLORS.length];
}

function normalizeIngredient(value: unknown): RecipeIngredient | null {
  if (!value || typeof value !== "object") {
    return null;
  }

  const ingredient = value as Partial<RecipeIngredient>;
  if (
    !ingredient.name ||
    typeof ingredient.name !== "string" ||
    typeof ingredient.amount !== "number" ||
    !Number.isFinite(ingredient.amount) ||
    typeof ingredient.unit !== "string" ||
    ingredient.unit.trim().length === 0
  ) {
    return null;
  }

  return {
    name: ingredient.name,
    amount: ingredient.amount,
    unit: ingredient.unit.trim(),
  };
}

function normalizeRecipe(input: Partial<Recipe>, index: number): Recipe {
  const normalizedIngredients = Array.isArray(input.ingredients)
    ? input.ingredients
        .map((value) => normalizeIngredient(value))
        .filter((value): value is RecipeIngredient => Boolean(value))
    : [];

  return {
    id: input.id || `generated-${Date.now()}-${index}`,
    title: input.title || "Neues Rezept",
    description: input.description || "KI-generiertes Rezept",
    cookingTime: input.cookingTime || "Unbekannt",
    category: input.category || "Allgemein",
    servings: Number(input.servings) || 2,
    color: input.color || selectCardColor(index),
    ingredients: normalizedIngredients,
    stepsList: Array.isArray(input.stepsList) ? input.stepsList : [],
    image: input.image || "",
  };
}

function loadStoredRecipes(): Recipe[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }

    const parsed = JSON.parse(raw) as Partial<Recipe>[];
    if (!Array.isArray(parsed)) {
      return [];
    }

    return parsed.map((item, index) => normalizeRecipe(item, index));
  } catch (error) {
    console.warn("Could not load stored recipes", error);
    return [];
  }
}

export default defineComponent({
  name: "RecipeTable",
  components: {
    RecipeRequest,
    RecipeDetail,
  },
  setup() {
    const $q: QVueGlobals = useQuasar();
    const dataStore = useDataStore();
    const { articles } = storeToRefs(dataStore);
    const showDialog = ref(false);
    const recipes = ref<Recipe[]>(loadStoredRecipes());
    const selectedRecipe = ref<Recipe | null>(null);

    const recipeInputArticles = computed(() =>
      [...articles.value].sort(
        (a, b) =>
          new Date(b.Purchase_Date).getTime() - new Date(a.Purchase_Date).getTime()
      )
    );

    const goToRecipe = (recipe: Recipe) => {
      selectedRecipe.value = recipe;
      showDialog.value = true;
    };

    const closeOverlay = () => {
      showDialog.value = false;
      selectedRecipe.value = null;
    };

    onBeforeRouteLeave(() => {
      closeOverlay();
    });

    const handleAddRecipe = (recipe: Recipe) => {
      const normalized = normalizeRecipe(recipe, recipes.value.length);
      recipes.value = [normalized, ...recipes.value];
    };

    watch(
      recipes,
      (newRecipes) => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(newRecipes));
      },
      { deep: true }
    );

    onMounted(async () => {
      try {
        await dataStore.ensureInitialized();
      } catch (error) {
        handleError("Daten laden", error, $q);
      }
      dataStore.startRealtime();
    });

    return {
      recipes,
      recipeInputArticles,
      goToRecipe,
      selectedRecipe,
      showDialog,
      closeOverlay,
      handleAddRecipe,
    };
  },
});
</script>

<style scoped lang="scss">
.cards {
  border-radius: 25px;
  height: auto;
  max-width: 320px;
}

.empty-state {
  max-width: 420px;
}

h5 {
  margin-block-start: 25px;
  margin-block-end: 5px;
}

.text-subtitle2 {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.btn-background {
  color: $primary;
  background-color: white !important;
}

.text-h6 {
  font-weight: bold;
  font-family: $font-playfair;
}

.btn {
  background-color: white !important;
}

.card-background1 {
  background-color: #e6f7e8;
}

.card-background2 {
  background-color: #f0f7ff;
}

.card-background3 {
  background-color: #fdf3e6;
}
</style>
