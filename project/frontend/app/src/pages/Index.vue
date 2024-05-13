<template>
  <q-page class="flex">
    <div class="q-pa-md">

    </div>
    <ScannerPage></ScannerPage>
    <FoodPage></FoodPage>
  </q-page>
</template>

<script>
import { defineComponent, ref } from "vue";
import ScannerPage from "./Scanner.vue";
import FoodPage from "./Food.vue";

export default defineComponent({
  name: "HomePage",
  components: {
    ScannerPage,
    FoodPage,
  },
  setup() {
    const stores = ref([
      {
        name: "Coop",
        steps: [
          {
            icon: "login",
            description:
              "Besuchen Sie die Supercard-Webseite und loggen Sie sich ein.",
            url: "https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html",
          },
          {
            icon: "map",
            description:
              "Navigieren Sie zu 'App & Digitale Services' > 'Meine Einkäufe'.",
          },
          {
            icon: "file_download",
            description:
              "Laden Sie die gewünschten Coop Einkaufsbelege (PDF) auf Ihr Gerät.",
          },
          {
            icon: "scanner",
            description:
              "Navigieren Sie in der MyMenu App zum Scanner und laden Sie die PDFs hoch.",
          },
        ],
      },
      {
        name: "Migros",
        steps: [
          {
            icon: "login",
            description:
              "Besuchen Sie die Migros-Webseite und loggen Sie sich ein.",
            url: "https://cumulus.migros.ch/de/konto/kassenbons.html",
          },
          {
            icon: "map",
            description: "Navigieren Sie zu 'Cumulus Konto' > 'Kassenbons'.",
          },
          {
            icon: "file_download",
            description:
              "Laden Sie die gewünschten Migros Einkaufsbelege (PDF) auf Ihr Gerät.",
          },
          {
            icon: "scanner",
            description:
              "Navigieren Sie in der MyMenu App zum Scanner und laden Sie die PDFs hoch.",
          },
        ],
      },
    ]);

    return {
      stores,
    };
  },
});
</script>

<style scoped lang="scss">
.my-card {
  width: 90%;
  max-width: 700px;
}

.bg-primary {
  background-color: $primary !important;
}

.text-primary {
  color: $positive !important;
}

.rounded-borders {
  border-radius: 8px;
}
</style>
