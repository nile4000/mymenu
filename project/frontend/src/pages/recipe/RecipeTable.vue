<template>
  <div class="q-pa-md row justify-evenly">
    <RecipeRequest />
    <h5 style="margin-block-end: 30px; margin-block-start: 30px">Deine Rezepte</h5>
    <div
      class="row q-gutter-md justify-center"
      style="padding-left: 6px; padding-bottom: 8px"
    >
      <q-card
        flat
        bordered
        v-for="recipe in yourRecipes"
        :key="recipe.id"
        class="q-ma-sm cards"
        :class="recipe.color"
      >
        <q-card-section  class="row justify-center">
          <div class="text-h6" style="padding-bottom: 8px;">{{ recipe.title }}</div>
          <div
            class="text-subtitle2"
            style="
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            "
          >
            {{ recipe.description }}
          </div>
          <div>
            <q-icon name="schedule" /> {{ recipe.cookingTime }} |
            <q-icon name="category" /> {{ recipe.category }} |
            <q-icon name="groups" /> {{ recipe.servings }}
          </div>
        </q-card-section>
        <q-card-actions align="center" class="q-px-md">
          <q-btn flat round icon="hub" class="btn-background"></q-btn>
          <q-btn
            flat
            round
            color="primary"
            icon="north_east"
            class="btn-background"
            @click="goToRecipe(recipe)"
          ></q-btn>
        </q-card-actions>
      </q-card>
    </div>
    <template v-if="selectedRecipe">
      <RecipeDetail
        :recipe="selectedRecipe"
        @close="selectedRecipe = null"
      ></RecipeDetail>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import { useQuasar } from "quasar";
import RecipeRequest from "../../components/RecipeRequest.vue";
import RecipeDetail from "./RecipeDetail.vue";

export default defineComponent({
  name: "RecipeTable",
  components: {
    RecipeRequest,
    RecipeDetail,
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
        cookingTime: "75 Min",
        category: "Vegetarisch",
        servings: 6,
        color: "card-background1",
        ingredients: ["Lasagne-Blätter", "Gemüse", "Käse"],
        stepsList: [
          "Gemüse schneiden.",
          "Soße zubereiten.",
          "Lasagne schichten und backen.",
        ],
        image: "https://via.placeholder.com/300",
      },
      {
        id: 5,
        title: "Kürbiscremesuppe",
        description: "Eine cremige Suppe aus Hokkaido-Kürbis und Kokosmilch.",
        cookingTime: "40 Min",
        category: "Vegan",
        servings: 4,
        color: "card-background2",
        ingredients: ["Hokkaido-Kürbis", "Kokosmilch", "Gewürze"],
        stepsList: [
          "Kürbis vorbereiten.",
          "Suppe kochen.",
          "Abschmecken und servieren.",
        ],
        image: "https://via.placeholder.com/300",
      },
      {
        id: 6,
        title: "Quinoa-Salat mit Avocado",
        description:
          "Ein leichter Salat mit Quinoa, Avocado und frischem Gemüse.",
        cookingTime: "25 Min",
        category: "Gesund",
        servings: 2,
        color: "card-background3",
        ingredients: ["Quinoa", "Avocado", "Gemüse"],
        stepsList: [
          "Quinoa kochen.",
          "Gemüse schneiden.",
          "Alles mischen und würzen.",
        ],
        image: "https://via.placeholder.com/300",
      },
    ]);

    const selectedRecipe = ref(null);

    const goToRecipe = (recipe: any) => {
      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
      selectedRecipe.value = recipe;
      console.log(selectedRecipe.value);
    };

    return {
      $q,
      activeTab,
      selectedOption,
      yourRecipes,
      goToRecipe,
      selectedRecipe,
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
