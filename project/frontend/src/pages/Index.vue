<template>
  <q-page>
    <!-- <q-list padding bordered class="rounded-borders q-ma-md">
      <q-expansion-item
        icon="help"
        v-for="store in stores"
        :key="store.name"
        expand-separator
        :label="'Einkaufsbelege bei ' + store.name + ' abrufen'"
      >
        <q-card class="my-card">
          <q-card-section>
            <q-list bordered separator class="rounded-borders">
              <q-item dense v-for="step in store.steps" :key="step.description">
                <q-item-section side>
                  <q-icon :name="step.icon"></q-icon>
                </q-item-section>
                <q-item-section>
                  <span v-if="step.url">
                    <span>{{ getPrefix(step.description) }}</span>
                    <a class="text-primary" :href="step.url" target="_blank">{{
                      getLinkText(step.description)
                    }}</a>
                    <span>{{ getSuffix(step.description) }}</span>
                  </span>
                  <span v-else>{{ step.description }}</span>
                </q-item-section>
              </q-item>
            </q-list>
          </q-card-section>
        </q-card>
      </q-expansion-item>
    </q-list> -->
    <ScannerPage></ScannerPage>
    <FoodPage></FoodPage>
  </q-page>
</template>


<script lang="ts">
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

    const getPrefix = (description: any) => {
      const match = description.match(/^(.*?)(Supercard-Webseite|Migros-Webseite)/);
      return match ? match[1] : '';
    };

    const getLinkText = (description: any) => {
      const match = description.match(/(Supercard-Webseite|Migros-Webseite)/);
      return match ? match[1] : '';
    };

    const getSuffix = (description: any) => {
      const match = description.match(/(Supercard-Webseite|Migros-Webseite)(.*)$/);
      return match ? match[2] : '';
    };

    return {
      stores,
      getPrefix,
      getLinkText,
      getSuffix,
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
