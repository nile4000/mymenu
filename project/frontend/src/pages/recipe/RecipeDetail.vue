<template>
  <div class="q-ma-sm recipe-detail-page">
    <!-- Zurück-Button -->
    <div class="back-button">
      <q-btn flat round icon="close" color="primary" @click="$emit('close')" />
    </div>

    <!-- Rezept-Titel -->
    <div class="recipe-title-container">
      <h3 class="recipe-title">{{ recipe.title }}</h3>

      <p class="recipe-description">{{ recipe.description }}</p>
    </div>

    <!-- Rezept-Details -->
    <div class="recipe-details">
      <div class="detail-item">
        <q-icon size="xs" name="schedule" class="detail-icon" color="negative" />
        <span>{{ recipe.cookingTime }}</span>
      </div>
      <div class="detail-item">
        <q-icon size="xs" name="room_service" class="detail-icon" color="amber"/>
        <span>{{ recipe.stepsList.length }} Steps</span>
      </div>
      <div class="detail-item">
        <q-icon size="xs" name="menu_book" class="detail-icon" color="secondary" />
        <span>{{ recipe.category }}</span>
      </div>
    </div>

    <!-- Inhaltsabschnitt -->
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
              v-for="ingredient in recipe.ingredients"
              :key="ingredient"
            >
              <q-icon name="restaurant" class="ingredient-icon" />
              <span>{{ ingredient }}</span>
            </div>
          </div>
        </q-tab-panel>
        <q-tab-panel name="steps">
          <div class="steps-list">
            <p v-for="(step, index) in recipe.stepsList" :key="index">
              {{ index + 1 }}. {{ step }}
            </p>
          </div>
        </q-tab-panel>
      </q-tab-panels>
    </div>
  </div>
</template>
<script lang="ts">
import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "RecipeDetail",
  props: {
    recipe: {
      type: Object,
      required: false,
    },
  },
  setup() {
    const activeTab = ref("ingredients");

    return {
      activeTab,
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
  max-width: 400px;
  border-radius: 16px;
  margin: 10px;
  padding-top: 0px;
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
  max-width: 320px;
  margin: 0px;
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
  max-width: 300px;
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
  margin-bottom: 12px;
  margin-top: 12px;

}

.q-tab {
  text-transform: none;
}

::v-deep .q-tab--active {
  margin: 2px;
  background-color: $bar-background;
  border-radius: 25px;
}

.q-tab-panel {
  padding-bottom: 0px;
}

.q-tab-panels {
  border-radius: 15px;
  border: 1px solid $primary;
  margin-block-end: 8px;
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
