<template>
  <router-view />
</template>

<script>
import { defineComponent } from "vue";
import { useMeta } from "quasar";

export default defineComponent({
  name: "App",
  setup() {
    const metaData = {
      title: "MyMenü",
      titleTemplate: () => `MyMenü - AI Menü Planer`,
      meta: {
        keywords: { name: "keywords", content: "AI Menü Planer | lueem.dev | Einkaufsbeleg Scanner | Coop | Migros" },
        equiv: {
          "http-equiv": "Content-Type",
          content: "text/html; charset=UTF-8",
        },
        ogTitle: {
          property: "og:title",
          template() {
            return `MyMenu - Menu Planner`;
          },
        },
      },
    };

    // defines meta
    useMeta(metaData);

    return {};
  },
});
</script>
