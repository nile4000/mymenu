<template>
  <div>
    <div class="q-pa-md row justify-center">
      <RecipeRequest />
      <h5>Rezepte</h5>
    </div>
    <div class="row justify-center">
      <q-card
        flat
        bordered
        v-for="recipe in yourRecipes"
        :key="recipe.id"
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
            <q-tooltip anchor="center left" class="text-h6">Ändern</q-tooltip>
          </q-btn>
          <q-btn
            flat
            round
            color="primary"
            icon="north_east"
            class="btn-background"
            @click="goToRecipe(recipe)"
            ><q-tooltip anchor="center left" class="text-h6">Details</q-tooltip>
          </q-btn>
        </q-card-actions>
      </q-card>
      <template v-if="selectedRecipe">
        <!-- Overlay für Rezeptdetails -->
        <q-dialog
          v-model="showDialog"
          persistent
          transition-show="slide-up"
          transition-hide="slide-down"
        >
          <RecipeDetail
            v-if="selectedRecipe"
            :recipe="selectedRecipe"
            @close="closeOverlay"
          />
        </q-dialog>
      </template>
    </div>
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
    const selectedOption = ref(null);
    const showDialog = ref(false);

    const yourRecipes = ref([
      {
        id: 4,
        title: "Vegetarische Lasagne",
        description: "Eine köstliche Lasagne mit viel Gemüse und Käse.",
        cookingTime: "75 Min",
        category: "Vegetarisch",
        servings: 6,
        color: "card-background1",
        ingredients: [
          "12 Lasagne-Blätter",
          "500g gemischtes Gemüse (z.B. Zucchini, Paprika, Karotten)",
          "300g geriebener Käse",
          "1 Zwiebel",
          "2 Knoblauchzehen",
          "400ml Tomatensoße",
          "2 EL Olivenöl",
          "Salz und Pfeffer nach Geschmack",
        ],
        stepsList: [
          "Gemüse schneiden.",
          "Zwiebel und Knoblauch anbraten.",
          "Soße mit Gemüse zubereiten.",
          "Lasagne schichten und mit Käse bestreuen.",
          "Im Ofen bei 180°C ca. 45 Minuten backen.",
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
        ingredients: [
          "1 großer Hokkaido-Kürbis (ca. 1kg)",
          "400ml Kokosmilch",
          "1 Zwiebel",
          "2 Knoblauchzehen",
          "1 Liter Gemüsebrühe",
          "2 EL Olivenöl",
          "Salz und Pfeffer nach Geschmack",
          "Gewürze nach Wahl (z.B. Muskat, Ingwer)",
        ],
        stepsList: [
          "Kürbis vorbereiten und würfeln.",
          "Zwiebel und Knoblauch anbraten.",
          "Kürbis hinzufügen und kurz mitbraten.",
          "Mit Gemüsebrühe aufgießen und köcheln lassen.",
          "Kokosmilch hinzufügen und pürieren.",
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
        ingredients: [
          "200g Quinoa",
          "1 reife Avocado",
          "300g gemischtes Gemüse (z.B. Gurken, Tomaten, Paprika)",
          "50g frischer Spinat",
          "2 EL Olivenöl",
          "1 Zitrone (Saft)",
          "Salz und Pfeffer nach Geschmack",
          "Frische Kräuter (z.B. Petersilie, Koriander)",
        ],
        stepsList: [
          "Quinoa nach Packungsanleitung kochen und abkühlen lassen.",
          "Gemüse schneiden.",
          "Avocado würfeln.",
          "Alles in einer Schüssel mischen.",
          "Mit Olivenöl, Zitronensaft, Salz und Pfeffer würzen.",
          "Mit frischen Kräutern garnieren und servieren.",
        ],
        image: "https://via.placeholder.com/300",
      },
    ]);

    const selectedRecipe = ref(null);

    const goToRecipe = (recipe: any) => {
      selectedRecipe.value = recipe;
      showDialog.value = true;
    };

    const closeOverlay = () => {
      showDialog.value = false;
      selectedRecipe.value = null;
    };

    return {
      $q,
      selectedOption,
      yourRecipes,
      goToRecipe,
      selectedRecipe,
      showDialog,
      closeOverlay,
      standard: ref(2),
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
