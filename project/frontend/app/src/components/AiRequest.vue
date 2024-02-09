<template>
  <div>
    <q-btn color="positive" label="Menü erstellen" @click="sendRequest"></q-btn>
    <div v-if="recipe">
      <q-card class="q-ma-md">
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
      </q-card>
    </div>
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
      recipe: null, // Speichert das Rezept nach dem Laden
    };
  },
  methods: {
    prepareArticles(selectedItems) {
      return selectedItems.map((item) => item.Name);
    },

    async sendRequest() {
      let articleNames = this.prepareArticles(this.selectedItems);
      try {
        const response = await axios.post(
          "http://localhost:8080/api/ai-request",
          {
            articleNames,
          },
          {
            headers: {
              Accept: "application/json",
              "Content-Type": "application/json",
              Firebaseauthid: getAuth().currentUser.uid || null,
            },
          }
        );

        this.recipe = response.data[0]; // Nehme das erste Rezept aus der Antwort
      } catch (error) {
        console.error("Fehler beim Senden der Anfrage:", error);
      }
    },
  },
});
</script>
