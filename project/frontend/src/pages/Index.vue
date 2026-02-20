<template>
  <q-page>
    <ArticleTable />
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import ArticleTable from "./article/ArticleTable.vue";

export default defineComponent({
  name: "IndexPage",
  components: {
    ArticleTable,
  },
});
</script>
