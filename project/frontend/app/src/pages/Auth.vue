<template>
  <q-page class="flex q-pa-md justify-center items-center">
    <q-card style="width: 100%; max-width: 500px" class="">
      <q-tabs
        v-model="tab"
        dense
        class="text-grey-7 bg-grey-3"
        active-color="primary"
        indicator-color="primary"
        align="justify"
        switch-indicator
        active-bg-color="white"
      >
        <q-tab name="login" label="Einloggen" />
        <q-tab name="register" label="Registrieren" />
      </q-tabs>

      <q-tab-panels v-model="tab" animated>
        <q-tab-panel name="login" class="q-pa-none">
          <AuthComponent :tab="tab" />
        </q-tab-panel>

        <q-tab-panel name="register" class="q-pa-none">
          <AuthComponent :tab="tab" />
          <div class="q-ma-sm">
            <b>Hinweis:</b> MyMenu speichert keine Einkaufsdaten in unseren Systemen.
          </div>
        </q-tab-panel>
      </q-tab-panels>
    </q-card>
  </q-page>
</template>

<script lang="ts">
// import AuthComponent from "components/AuthComponent";
import { defineComponent, ref } from "vue";

export default defineComponent({
  name: "AuthComp",
  // components: { AuthComponent },
  setup() {
    const tab = ref("login");
    return {
      tab,
    };
  },
});
</script>
