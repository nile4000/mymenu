<template>
  <div class="q-pa-md">
    <q-tabs v-model="activeTab" class="text-primary">
      <q-tab name="recommended" label="Empfohlene Rezepte" />
      <q-tab name="yours" label="Deine Rezepte" />
    </q-tabs>

    <q-separator />

    <div v-if="activeTab === 'recommended'" class="q-pa-md row q-gutter-md">
      <!-- Empfohlene Rezepte Karten -->
      <q-card
        v-for="recipe in recommendedRecipes"
        :key="recipe.id"
        class="q-ma-sm"
        style="width: 200px"
      >
        <q-card-section>
          <div class="text-h6">{{ recipe.title }}</div>
        </q-card-section>
      </q-card>
    </div>

    <div v-else class="q-pa-md">
      <!-- Auswahl unter "Deine Rezepte" -->
      <q-option-group
        v-model="selectedOption"
        :options="options"
        type="radio"
        @update:model-value="onOptionChange"
      ></q-option-group>

      <!-- Deine Rezepte Karten -->
      <div class="row q-gutter-md">
        <q-card
          v-for="recipe in filteredYourRecipes"
          :key="recipe.id"
          class="q-ma-sm"
          style="width: 200px"
        >
          <q-card-section>
            <div class="text-h6">{{ recipe.title }}</div>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { useQuasar } from 'quasar';

export default defineComponent({
  name: 'ReceipeTable',
  setup() {
    const $q = useQuasar();
    const activeTab = ref('recommended');
    const selectedOption = ref(null);

    const options = [
      { label: 'Ich möchte abnehmen', value: 'abnehmen' },
      { label: 'Ich möchte Muskeln aufbauen und Fett abbauen', value: 'muskeln' },
      { label: 'Ich möchte mich gesünder ernähren', value: 'gesuender' },
    ];

    const recommendedRecipes = ref([
      { id: 1, title: 'Rezept A' },
      { id: 2, title: 'Rezept B' },
      { id: 3, title: 'Rezept C' },
    ]);

    const yourRecipes = ref([
      { id: 4, title: 'Dein Rezept 1', category: 'abnehmen' },
      { id: 5, title: 'Dein Rezept 2', category: 'muskeln' },
      { id: 6, title: 'Dein Rezept 3', category: 'gesuender' },
      { id: 7, title: 'Dein Rezept 4', category: 'abnehmen' },
    ]);

    const filteredYourRecipes = computed(() => {
      if (!selectedOption.value) {
        return yourRecipes.value;
      }
      return yourRecipes.value.filter(
        (recipe) => recipe.category === selectedOption.value
      );
    });

    function onOptionChange() {
      // Hier kannst du zusätzliche Logik hinzufügen, wenn sich die Auswahl ändert
    }

    return {
      $q,
      activeTab,
      selectedOption,
      options,
      recommendedRecipes,
      yourRecipes,
      filteredYourRecipes,
      onOptionChange,
    };
  },
});
</script>

<style scoped lang="scss">
</style>
