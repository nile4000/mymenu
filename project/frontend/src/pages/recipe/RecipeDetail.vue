<template>
  <div class="q-pa-md recipe-detail-page">
    <!-- Zurück-Button -->
    <div class="back-button">
      <q-btn flat round icon="arrow_back" color="primary" @click="$emit('close')" />
    </div>

    <!-- Bild des Rezepts -->
    <div class="recipe-image-container">
      <img
        :src="recipe.image || 'https://via.placeholder.com/300'"
        :alt="recipe.title || 'Recipe'"
        class="recipe-image"
      />
    </div>

    <!-- Rezept-Titel -->
    <div class="recipe-title-container">
      <h3 class="recipe-title">{{ recipe.title }}</h3>
      <div class="author-container">
        <q-avatar rounded size="50px" class="author-avatar">
          <img src="https://via.placeholder.com/50" alt="Author" />
        </q-avatar>
        <div class="author-info">
          <span class="author-name">Syed Raju</span>
          <span class="author-recipes">(10 recipes)</span>
        </div>
        <q-btn flat round icon="favorite" color="red" class="favorite-button" />
      </div>
    </div>

    <!-- Rezept-Details -->
    <div class="recipe-details">
      <div class="detail-item">
        <q-icon name="timer" class="detail-icon" />
        <span>{{ recipe.cookingTime }}</span>
      </div>
      <div class="detail-item">
        <q-icon name="list_alt" class="detail-icon" />
        <span>{{ recipe.stepsList.length }} Steps</span>
      </div>
      <div class="detail-item">
        <q-icon name="chef_hat" class="detail-icon" />
        <span>{{ recipe.category }}</span>
      </div>
    </div>

    <!-- Inhaltsabschnitt -->
    <div class="ingredients-section">
      <q-tabs v-model="activeTab" class="ingredients-tabs">
        <q-tab name="ingredients" label="Ingredients" />
        <q-tab name="steps" label="Steps" />
      </q-tabs>

      <q-tab-panels v-model="activeTab">
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
      required: true,
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


<style scoped>
.recipe-detail-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f5f5f5;
  padding: 16px;
  border-radius: 16px;
}

.back-button {
  align-self: flex-start;
  margin-bottom: 16px;
}

.recipe-image-container {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
}

.recipe-image {
  width: 100%;
  border-radius: 50%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.recipe-title-container {
  text-align: center;
  margin: 16px 0;
}

.recipe-title {
  font-size: 24px;
  font-weight: bold;
  margin: 8px 0;
}

.author-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 8px 0;
}

.author-avatar {
  margin-right: 8px;
}

.author-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-right: auto;
}

.favorite-button {
  margin-left: 16px;
}

.recipe-details {
  display: flex;
  justify-content: space-around;
  width: 100%;
  max-width: 300px;
  margin: 16px 0;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ingredients-section {
  width: 100%;
  max-width: 300px;
}

.ingredients-tabs {
  margin-bottom: 16px;
}

.ingredient-list,
.steps-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.ingredient-icon {
  color: #4caf50;
}
</style>
