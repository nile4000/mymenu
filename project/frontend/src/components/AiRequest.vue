<template>
  <div>
    <q-btn
      color="secondary"
      :label="`Kategorisieren (${selectedItems.length})`"
      icon="menu_book"
      @click="sendRequest"
      :disabled="selectedItems.length === 0 || isLoading"
      v-ripple
      style="margin: 20px"
    ></q-btn>

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
import { updateArticleById } from "../services/updateArticle";
import { useQuasar } from "quasar"; // Importiere useQuasar

export default defineComponent({
  name: "AiRequest",
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  setup(props) {
    const $q = useQuasar();

    const sendRequest = async () => {
      if (props.selectedItems.length === 0) {
        $q.notify({
          type: "warning",
          message: "Bitte wählen Sie mindestens einen Artikel aus.",
        });
        return;
      }

      const batchSize = 50;
      const articles = prepareArticles(props.selectedItems);

      const batches = [];
      for (let i = 0; i < articles.length; i += batchSize) {
        batches.push(articles.slice(i, i + batchSize));
      }

      // Ladebildschirm anzeigen
      $q.loading.show({
        message: "Kategorisierung läuft...",
        backgroundColor: "rgba(0, 0, 0, 0.5)",
        spinnerColor: "white",
      });

      try {
        for (const batch of batches) {
          const prompt = `Categorize the following articles based on their names:\n${batch
            .map((article) => `ID: ${article.id}, Name: ${article.name}`)
            .join(
              "\n"
            )}\n\nUse the following categories for classification:\n1. Obst und Gemüse\n2. Milchprodukte, Eier und Alternativen\n3. Fleisch, Fisch und pflanzliche Proteine\n4. Backwaren und Getreide\n5. Softgetraenke\n6. Alkoholische Getraenke\n7. Snacks und Suesswaren\n8. Reinigungsmittel und Haushaltsreiniger\n9. Koerperpflegeprodukte und Hygieneartikel\n10. Tierbedarf und Sonstiges\nIf an article does not fit into any of these categories, assign it the category 'Andere'.`;

          const response = await axios.post(
            "https://api.openai.com/v1/chat/completions",
            {
              model: "gpt-4o-mini",
              messages: [
                {
                  role: "system",
                  content:
                    "You are a categorization-assistant that provides information in JSON format. Return only pure JSON without any code block or formatting, adhering strictly to the following structure: [{id: string, category: string}].",
                },
                {
                  role: "user",
                  content: prompt,
                },
              ],
            },
            {
              headers: {
                Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
                "Content-Type": "application/json",
              },
            }
          );

          // Sanitisierung, Formatierung, Parsing
          let categorizedData = response.data.choices[0].message.content;
          categorizedData = categorizedData.replace(/```json|```/g, "");
          const categorizedArticles = JSON.parse(categorizedData);

          const validCategorizedArticles = categorizedArticles.filter(
            (article: { id: string; category: string }) =>
              article.category && article.category.trim() !== ""
          );

          if (validCategorizedArticles.length > 0) {
            await updateArticleById(validCategorizedArticles);
          }
        }

        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
      } catch (error) {
        $q.notify({
          type: "negative",
          message: "Fehler bei der Kategorisierung.",
        });
      } finally {
        $q.loading.hide();
      }
    };

    const openHistory = () => {
      void router.push("/receipt");
    };

    const prepareArticles = (selectedItems: Article[]) => {
      return selectedItems.map((item: Article) => ({
        id: item.Id,
        name: item.Name,
      }));
    };

    return {
      sendRequest,
      openHistory,
      prepareArticles,
    };
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
