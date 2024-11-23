<template>
  <div class="q-pa-md">
    <ReceipeRequest />
    <h4>Deine Rezepte</h4>
    <div class="row q-gutter-md">
      <q-card
        flat
        bordered
        v-for="recipe in yourRecipes"
        :key="recipe.id"
        class="q-ma-sm cards"
        :class="recipe.color"
      >
        <q-card-section style="padding-bottom: 0px;">
          <div class="text-h6">{{ recipe.title }}</div>
          <div class="text-subtitle2">{{ recipe.description }}</div>
          <div><q-icon name="timer" /> {{ recipe.cookingTime }}</div>
          <div><q-icon name="category" /> {{ recipe.category }}</div>
          <div><q-icon name="groups" /> {{ recipe.servings }}</div>
        </q-card-section>
        <q-card-actions align="center" class="q-px-md" >
          <q-btn flat round color="red" icon="favorite" class="btn-background"></q-btn>
          <q-btn flat round color="primary" icon="north_east" class="btn-background" ></q-btn>
        </q-card-actions>
      </q-card>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import { useQuasar } from "quasar";
import ReceipeRequest from "../../components/ReceipeRequest.vue";

export default defineComponent({
  name: "ReceipeTable",
  components: {
    ReceipeRequest,
  },
  setup() {
    const $q = useQuasar();
    const activeTab = ref("yours");
    const selectedOption = ref(null);

    const yourRecipes = ref([
      {
        id: 4,
        title: "Vegetarische Lasagne",
        description: "Eine köstliche Lasagne mit viel Gemüse und Käse.",
        cookingTime: "1 Stunde 15 Minuten",
        category: "Vegetarisch",
        servings: 6,
        color: "card-background1",
      },
      {
        id: 5,
        title: "Kürbiscremesuppe",
        description: "Eine cremige Suppe aus Hokkaido-Kürbis und Kokosmilch.",
        cookingTime: "40 Minuten",
        category: "Vegan",
        servings: 4,
        color: "card-background2",
      },
      {
        id: 6,
        title: "Quinoa-Salat mit Avocado",
        description:
          "Ein leichter Salat mit Quinoa, Avocado und frischem Gemüse.",
        cookingTime: "25 Minuten",
        category: "Gesund",
        servings: 2,
        color: "card-background3",
      },
    ]);

    function onOptionChange() {
      // Zusätzliche Logik bei Änderung der Auswahl
    }

    return {
      $q,
      activeTab,
      selectedOption,
      yourRecipes,
      onOptionChange,
    };
  },
});
</script>

<style scoped lang="scss">
.cards {
  border-radius: 25px;
  height: auto;
  max-height: 200px;
}

.btn-background {
  background-color: white !important;
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
