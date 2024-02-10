<template>
  <q-page class="flex flex-center">
    <q-card class="q-ma-md my-card" v-for="store in stores" :key="store.name">
      <q-card-section class="bg-primary text-white">
        <div class="text-h5">Einkaufsbelege bei {{ store.name }} abrufen</div>
      </q-card-section>
      <q-card-section>
        <q-list bordered separator class="rounded-borders">
          <q-item dense v-for="step in store.steps" :key="step.description">
            <q-item-section side>
              <q-icon :name="step.icon"></q-icon>
            </q-item-section>
            <q-item-section>
              <span v-if="step.url">
                <a class="text-primary" :href="step.url" target="_blank">{{
                  step.description
                }}</a>
              </span>
              <span v-else>{{ step.description }}</span>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script>
import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "HomePage",
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
