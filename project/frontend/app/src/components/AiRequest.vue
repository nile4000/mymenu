<template>
  <q-btn color="positive" label="Menü erstellen" @click="sendRequest"></q-btn>
  <div v>
    <q-card class="q-ma-md">
      <q-inner-loading class="card-example" :showing="isLoading" label="Lädt.."></q-inner-loading>
      <div v-if="!isLoading && recipe">
        <q-card-section>
          <div class="text-h6">{{ recipe.Name }}</div>
        </q-card-section>
        <q-card-section>
          <div><b>Zutaten:</b> {{ recipe.Ingredients }}</div>
        </q-card-section>
        <q-card-section>
          <div><b>Zubereitung:</b> {{ recipe.Instructions }}</div>
        </q-card-section>
        <q-card-section>
          <div><b>Portionen:</b> {{ recipe.Servings }}</div>
          <div><b>Zubereitungszeit:</b> {{ recipe.PreparationTime }}</div>
        </q-card-section>
      </div>
    </q-card>
  </div>
</template>

<script>
import axios from "axios";
import { getAuth } from "firebase/auth";
import { defineComponent } from "vue";

export default defineComponent({
  name: "AiRequest",
  props: {
    selectedItems: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      recipe: null,
      isLoading: false,
    };
  },
  methods: {
    prepareArticles(selectedItems) {
      return selectedItems.map((item) => item.Name);
    },

    async sendRequest() {
      this.isLoading = true;
      let articleNames = this.prepareArticles(this.selectedItems);
      try {
        const apiUrl = process.env.API_URL;
        const response = await axios.post(
          `${apiUrl}/api/ai-request`,
          {
            articleNames,
          },
          {
            headers: {
              Accept: "application/json",
              "Content-Type": "application/json",
            },
          }
        );

        this.recipe = response.data[0];
      } catch (error) {
        console.error("Fehler beim Senden der Anfrage:", error);
      } finally {
        this.isLoading = false;
      }
    },
  },
});
</script>
<style scoped>
.card-example {
  max-width: 400px;
  margin: 0 auto;
}
</style>
