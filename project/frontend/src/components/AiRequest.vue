<template>
  <div>
    <q-btn
      color="secondary"
      label="Kategorisieren"
      icon="menu_book"
      @click="sendRequest"
      :disabled="selectedItems.length === 0 || isLoading"
      v-ripple
      style="margin: 20px"
    ></q-btn>

    <q-inner-loading
      class="card-example"
      :showing="isLoading"
      label="Lädt"
    ></q-inner-loading>
    <q-btn
      color="primary"
      label="Beleg Übersicht"
      icon="dashboard"
      @click="openHistory"
      v-ripple
      style="margin: 20px"
    ></q-btn>
  </div>
</template>

<script lang="ts">
import axios from "axios";
import { Article } from "../helpers/interfaces/article.interface";
import router from "../router";
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "AiRequest",
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  data() {
    return {
      receipt: null,
      isLoading: false,
    };
  },

  methods: {
    prepareArticles(selectedItems: Article[]) {
      return selectedItems.map((item: Article) => ({
        id: item.Id,
        name: item.Name,
      }));
    },

    openHistory() {
      void router.push("/receipt");
    },

    sendRequest() {
      if (this.selectedItems.length === 0) {
        this.$q.notify({
          type: "warning",
          message: "Bitte wählen Sie mindestens einen Artikel aus.",
        });
        return;
      }

      // this.isLoading = true;
      const articles = this.prepareArticles(this.selectedItems);
      console.log(articles);
      // try {
      //   const apiUrl = process.env.VUE_APP_API_URL || "http://localhost:3000";
      //   const response = await axios.post(
      //     `${apiUrl}/api/ai-request`,
      //     {
      //       articles,
      //     },
      //     {
      //       headers: {
      //         Accept: "application/json",
      //         "Content-Type": "application/json",
      //       },
      //     }
      //   );

      //   this.receipt = response.data;
      //   this.$q.notify({
      //     type: "positive",
      //     message: "Kategorisierung erfolgreich!",
      //   });
      // } catch (error) {
      //   console.error("Fehler beim Senden der Anfrage:", error);
      //   this.$q.notify({
      //     type: "negative",
      //     message: "Fehler bei der Kategorisierung.",
      //   });
      // } finally {
      //   this.isLoading = false;
      // }
    },
  },
});
</script>

<style scoped>
.card-example {
  max-width: 400px;
  margin: 0 auto;
}
.button-loading-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 20px;
}
::v-deep .q-icon {
  color: white !important;
}
</style>
