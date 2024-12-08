<template>
  <q-layout view="lHh Lpr fff" class="bg-grey-1 main-layout">
    <q-header
      class="bg-grey-1 text-grey-8"
      style="display: flex; align-items: center; justify-content: center; "
    >
      <q-img
        src="../assets/mymenu-logo.png"
        style="max-width: 200px"
      ></q-img>
    </q-header>
    <q-page-container class="page-container full-height">
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script>
import { defineComponent } from "vue";

export default defineComponent({
  name: "AuthLayout",

  setup() {
    return {};
  },
});
</script>
