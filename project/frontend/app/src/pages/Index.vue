<template>
  <q-page class="flex flex-center">
    <q-card class="my-card">
      <q-card-section class="bg-primary text-white">
        <div class="text-h5">Einkaufsbelege bei Supercard abrufen</div>
      </q-card-section>
      <q-card-section class="q-pt-none">
        <div class="q-my-md">
          Folgen Sie diesen Schritten, um Ihre Einkaufsbelege von Supercard zu
          abzuholen:
        </div>
        <q-list bordered separator class="rounded-borders">
          <q-item dense>
            <q-item-section side>
              <q-icon name="login"></q-icon>
            </q-item-section>
            <q-item-section>
              <span>
                Besuchen Sie die
                <a
                  class="text-primary"
                  href="https://www.supercard.ch/de/app-digitale-services/meine-einkaeufe.html"
                  target="_blank"
                  >Supercard-Webseite</a
                >
                und loggen Sie sich ein.
              </span>
            </q-item-section>
          </q-item>
          <q-item dense>
            <q-item-section side>
              <q-icon name="map"></q-icon>
            </q-item-section>
            <q-item-section>
              Navigieren Sie zu "App & Digitale Services" > "Meine Einkäufe".
            </q-item-section>
          </q-item>
          <q-item dense>
            <q-item-section side>
              <q-icon name="file_download"></q-icon>
            </q-item-section>
            <q-item-section>
              Laden Sie die gewünschten Coop-Einkaufsbelege (PDF) auf Ihr Gerät.
            </q-item-section>
          </q-item>
          <q-item dense>
            <q-item-section side>
              <q-icon name="scanner"></q-icon>
            </q-item-section>
            <q-item-section>
              <span>
                Navigieren Sie in der MyMenu App zum
                <router-link to="/scanner" class="text-primary"
                  >Scanner</router-link
                >
                und laden Sie die PDFs hoch.
              </span>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script>
import { defineComponent } from "vue";

export default defineComponent({
  name: "HomePage",
});
</script>
<style scoped lang="scss">
.my-card {
  max-width: 600px;
}

.bg-primary {
  background-color: $primary !important;
}

.text-primary {
  color: $positive !important;
  text-decoration: none;
  font-weight:400;
}

.text-h5 {
  color: $color-text !important;
}

.rounded-borders {
  border-radius: 8px;
}
</style>
