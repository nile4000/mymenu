<template>
  <div class="recipe-detail-page">
    <div class="back-button">
      <q-btn flat round icon="close" color="primary" @click="$emit('close')" />
    </div>
    <!-- recipe title -->
    <div class="recipe-title-container">
      <h3 class="recipe-title">{{ recipe?.title }}</h3>
      <p class="recipe-description">{{ recipe?.description }}</p>
    </div>
    <!-- recipe details -->
    <div class="recipe-details">
      <div class="detail-item">
        <q-icon
          size="xs"
          name="schedule"
          class="detail-icon"
          color="negative"
        />
        <span>{{ recipe?.cookingTime }}</span>
      </div>
      <div class="detail-item">
        <q-icon
          size="xs"
          name="room_service"
          class="detail-icon"
          color="amber"
        />
        <span>{{ recipe?.stepsList.length }} Schritte</span>
      </div>
      <div class="detail-item">
        <q-icon
          size="xs"
          name="menu_book"
          class="detail-icon"
          color="secondary"
        />
        <span>{{ recipe?.category }}</span>
      </div>
      <div class="detail-item">
        <q-icon size="xs" name="groups" class="detail-icon" color="primary" />
        <span>{{ recipe?.servings }} Personen</span>
      </div>
    </div>
    <!-- recipe content -->
    <div class="ingredients-section">
      <q-tabs
        active-color="dark"
        indicator-color="transparent"
        rounded
        dense
        v-model="activeTab"
        class="ingredients-tabs"
      >
        <q-tab flat round name="ingredients" label="Zutaten" />
        <q-tab flat round name="steps" label="So geht's" />
      </q-tabs>

      <q-tab-panels rounded dense v-model="activeTab" animated>
        <q-tab-panel name="ingredients">
          <div class="ingredient-list">
            <div
              class="ingredient-item"
              v-for="(ingredient, index) in recipe?.ingredients"
              :key="`${ingredient.name}-${index}`"
            >
              <q-icon name="restaurant" class="ingredient-icon" />
              <span>{{ formatIngredient(ingredient) }}</span>
            </div>
          </div>
        </q-tab-panel>
        <q-tab-panel name="steps">
          <div class="steps-list">
            <p v-for="(step, index) in recipe?.stepsList" :key="index">
              {{ index + 1 }}. {{ step }}
            </p>
          </div>
        </q-tab-panel>
      </q-tab-panels>
    </div>
  </div>
</template>
<script lang="ts">
import { defineComponent, PropType, ref } from "vue";
import { Recipe, RecipeIngredient } from "../../helpers/interfaces/recipe.interface";

export default defineComponent({
  name: "RecipeDetail",
  props: {
    recipe: {
      type: Object as PropType<Recipe | null>,
      required: false,
      default: null,
    },
  },
  setup() {
    const activeTab = ref("ingredients");

    function formatIngredient(ingredient: RecipeIngredient): string {
      return `${ingredient.amount} ${ingredient.unit} ${ingredient.name}`;
    }

    return {
      activeTab,
      formatIngredient,
    };
  },
});
</script>

<style scoped lang="scss">
.recipe-detail-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: $bar-background;
  border: 1px solid $dark;
  border-radius: 16px;
  width: 100%;
}

.q-tab {
  text-decoration: none;
  border-radius: 25px;
  margin: 2px;
}

.back-button {
  margin: 0px;
  margin-top: 8px;
  align-self: flex-end;
}

.recipe-title-container {
  text-align: center;
  max-width: 300px;
}

.recipe-title {
  font-weight: bold;
  font-style: italic;
  font-family: $font-playfair;
  font-size: 20px;
  margin-block-start: 0px;
  margin-block-end: 10px;
  line-height: 1.2;
}

.recipe-details {
  display: flex;
  justify-content: space-evenly;
  width: 100%;
  max-width: 360px;
  height: 60px;
  border-radius: 15px;
  background-color: white;
}

.detail-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 4px;
}

.ingredients-section {
  width: 100%;
  max-width: 300px;
}

.ingredients-tabs {
  background-color: white;
  border-radius: 25px;
  margin-bottom: 20px;
  margin-top: 12px;
}

.q-tab {
  text-transform: none;
}

.q-tab-panel {
  padding-bottom: 4px;
}

::v-deep .q-tab--active {
  margin: 3px;
  background-color: $bar-background;
  border-radius: 25px;
}

.q-tab-panels {
  border-radius: 15px;
  border: 1px solid $primary;
  margin-block-end:20px;
}

.ingredient-list,
.steps-list {
  display: flex;
  flex-direction: column;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.ingredient-icon {
  color: #4caf50;
}
</style>
