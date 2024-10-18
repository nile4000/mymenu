<template>
  <div>
    <q-btn
      color="primary"
      label="Beleg Übersicht"
      icon="dashboard"
      @click="openOverview"
      v-ripple
    ></q-btn>
    <q-btn
      color="secondary"
      :label="`Kategorisieren (${selectedItems.length})`"
      icon="menu_book"
      @click="sendCategorizationRequest"
      :disabled="selectedItems.length === 0 || isLoading"
      v-ripple
      style="margin: 20px"
    ></q-btn>
    <q-btn
      color="secondary"
      :label="`Einheit extrahieren (${selectedItems.length})`"
      icon="colorize"
      @click="sendDetailExtractionRequest"
      :disabled="selectedItems.length === 0 || isLoading"
      v-ripple
    ></q-btn>
  </div>
</template>

<script lang="ts">
import axios, { AxiosResponse } from "axios";
import { Article } from "../helpers/interfaces/article.interface";
import router from "../router";
import { defineComponent, PropType, ref } from "vue";
import {
  updateArticleCategoryById,
  updateArticleDetailsById,
} from "../services/updateArticle";
import { useQuasar } from "quasar";

// Utility function for delay
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export default defineComponent({
  name: "AiRequest",
  props: {
    selectedItems: {
      type: Array as PropType<Article[]>,
      default: () => [],
    },
  },
  setup(props) {
    const openAiUrl = "https://api.openai.com/v1/chat/completions";
    const openAiModel = "gpt-4o-mini";
    const $q = useQuasar();
    const isLoading = ref(false);

    // Gemeinsame Utility-Funktionen
    const prepareArticles = (selectedItems: Article[]) => {
      return selectedItems.map((item: Article) => ({
        id: item.Id,
        name: item.Name,
      }));
    };

    const createBatches = (articles: any[], batchSize: number) => {
      const batches = [];
      for (let i = 0; i < articles.length; i += batchSize) {
        batches.push(articles.slice(i, i + batchSize));
      }
      return batches;
    };

    const validateSelectedItems = () => {
      if (props.selectedItems.length === 0) {
        $q.notify({
          type: "warning",
          message: "Bitte wählen Sie mindestens einen Artikel aus.",
        });
        throw new Error("Keine ausgewählten Artikel.");
      }
    };

    const showLoading = (message: string) => {
      $q.loading.show({
        message: message,
        backgroundColor: "rgba(0, 0, 0, 0.5)",
        spinnerColor: "white",
      });
      isLoading.value = true;
    };

    const hideLoading = () => {
      $q.loading.hide();
      isLoading.value = false;
    };

    const handleError = (error: any) => {
      $q.notify({
        type: "negative",
        message:
          error.message ||
          "Ein unerwarteter Fehler ist bei der Anfrage aufgetreten.",
      });
    };

    // Generiert den Kategorisierungs-Prompt
    const generateCategorizationPrompt = (batch: any[]) => {
      return `Categorize the following articles based on their names:\n${batch
        .map((article) => `ID: ${article.id}, Name: ${article.name}`)
        .join(
          "\n"
        )}\n\nUse the following categories for classification:\n1. Obst und Gemüse\n2. Milchprodukte, Eier und Alternativen\n3. Fleisch, Fisch und pflanzliche Proteine\n4. Backwaren und Getreide\n5. Softgetraenke\n6. Alkoholische Getraenke\n7. Snacks, Apero und Suesswaren\n8. Reinigungsmittel und Haushaltsreiniger\n9. Koerperpflegeprodukte und Hygieneartikel\n10. Tierbedarf und Sonstiges\nIf an article does not fit into any of these categories, assign it the category 'Andere'. Use stritly the following format: [{id: string, category: string}].`;
    };

    // Generiert den Detailextraktions-Prompt
    const generateDetailExtractionPrompt = (batch: any[]) => {
      return `Extract unit information for the following articles based on their names:\n${batch
        .map((article) => `ID: ${article.id}, Name: ${article.name}`)
        .join(
          "\n"
        )}\n\nFor each article, provide the unit and id in JSON format: [{id: string, unit: string}]. Default to empty string if no unit is found.`;
    };

    const callOpenAiApi = async (prompt: string) => {
      return axios.post(
        openAiUrl,
        {
          model: openAiModel,
          messages: [
            {
              role: "system",
              content:
                "You are an assistant that provides information in JSON format. Return only pure JSON without any code block or formatting. ",
            },
            {
              role: "user",
              content: prompt,
            },
          ],
          // temperature: 0, // Set temperature to 0 for deterministic results
          max_tokens: 4000, // Adjust as needed
        },
        {
          headers: {
            Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
            "Content-Type": "application/json",
          },
          timeout: 10000,
        }
      );
    };

    // Parst die API-Antwort
    const parseApiResponse = (response: AxiosResponse<any, any>) => {
      if (
        !response.data ||
        !response.data.choices ||
        !Array.isArray(response.data.choices) ||
        response.data.choices.length === 0 ||
        !response.data.choices[0].message ||
        !response.data.choices[0].message.content
      ) {
        throw new Error("Ungültige API-Antwortstruktur.");
      }

      let data = response.data.choices[0].message.content;
      data = data.replace(/```json|```/g, "").trim();

      let parsedData: any[];
      try {
        parsedData = JSON.parse(data);
      } catch (parseError) {
        throw new Error("Fehler beim Parsen der API-Antwort.");
      }

      if (!Array.isArray(parsedData)) {
        throw new Error("API-Antwort ist kein Array.");
      }

      return parsedData;
    };

    const processBatch: any = async (
      batch: any[],
      promptGenerator: (batch: any[]) => string,
      updateFunction: (data: any[]) => Promise<void>,
      maxRetries = 2,
      retryDelay = 2000,
      attempt = 1
    ) => {
      const prompt = promptGenerator(batch);
      try {
        const response = await callOpenAiApi(prompt);
        const parsedData = parseApiResponse(response);
        await updateFunction(parsedData);
      } catch (error) {
        if (attempt < maxRetries) {
          console.warn(
            `Fehler Batch-Verarbeitung (Versuch ${attempt} von ${maxRetries}). Retry in ${retryDelay}ms...`,
            error
          );
          await delay(retryDelay);
          return processBatch(
            batch,
            promptGenerator,
            updateFunction,
            maxRetries,
            retryDelay,
            attempt + 1
          );
        } else {
          console.error("Maximale Anzahl von Versuchen erreicht.", error);
          throw error;
        }
      }
    };

    // Verarbeitet alle Batches mit begrenzter Parallelität
    const processAllBatches = async (
      batches: any[],
      promptGenerator: (batch: any[]) => string,
      updateFunction: (data: any[]) => Promise<void>
    ) => {
      const concurrencyLimit = 5;
      let index = 0;

      const executeBatches = async () => {
        const promises = [];
        while (index < batches.length && promises.length < concurrencyLimit) {
          const batch = batches[index++];
          promises.push(processBatch(batch, promptGenerator, updateFunction));
        }
        await Promise.all(promises);
        if (index < batches.length) {
          await executeBatches();
        }
      };

      await executeBatches();
    };

    const sendCategorizationRequest = async () => {
      try {
        validateSelectedItems();

        const articles = prepareArticles(props.selectedItems);
        const batches = createBatches(articles, 50);

        showLoading("Kategorisierung läuft...");

        await processAllBatches(
          batches,
          generateCategorizationPrompt,
          async (categorizedArticles: any[]) => {
            const validCategorizedArticles = categorizedArticles.filter(
              (article) =>
                article.id &&
                typeof article.id === "string" &&
                article.category &&
                typeof article.category === "string" &&
                article.category.trim() !== ""
            );

            if (validCategorizedArticles.length > 0) {
              await updateArticleCategoryById(validCategorizedArticles);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen kategorisierten Artikel gefunden.",
              });
            }
          }
        );

        $q.notify({
          type: "positive",
          message: "Kategorisierung erfolgreich!",
        });
      } catch (error) {
        handleError(error);
      } finally {
        hideLoading();
      }
    };

    const sendDetailExtractionRequest = async () => {
      try {
        validateSelectedItems();

        const articles = prepareArticles(props.selectedItems);
        const batches = createBatches(articles, 50);

        showLoading("Einheit extrahieren läuft...");

        await processAllBatches(
          batches,
          generateDetailExtractionPrompt,
          async (extractedDetails: any[]) => {
            const validExtractedDetails = validateExtractedDetails(
              extractedDetails,
              $q
            );

            if (validExtractedDetails.length > 0) {
              await updateArticleDetailsById(validExtractedDetails);
            } else {
              $q.notify({
                type: "warning",
                message: "Keine gültigen extrahierten Einheiten gefunden.",
              });
            }
          }
        );

        $q.notify({
          type: "positive",
          message: "Einheitsextraktion erfolgreich!",
        });
      } catch (error) {
        handleError(error);
      } finally {
        hideLoading();
      }
    };

    const validateExtractedDetails = (
      extractedDetails: { id: string; unit: string }[],
      $q: any
    ) => {
      const validExtractedDetails = extractedDetails.filter(
        (detail) =>
          detail.id &&
          typeof detail.id === "string" &&
          (typeof detail.unit === "string" || detail.unit === "")
      );

      if (validExtractedDetails.length === 0) {
        $q.notify({
          type: "warning",
          message: "Keine gültigen extrahierten Einheiten gefunden.",
        });
        throw new Error("Ungültige extrahierte Einheiten.");
      }

      return validExtractedDetails;
    };

    const openOverview = () => {
      void router.push("/receipt");
    };

    return {
      sendCategorizationRequest,
      sendDetailExtractionRequest,
      openOverview,
      isLoading,
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
