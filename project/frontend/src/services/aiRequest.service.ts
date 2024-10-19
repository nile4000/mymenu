import axios, { AxiosResponse } from "axios";
import { Article } from "../helpers/interfaces/article.interface";

const OPEN_AI_URL = "https://api.openai.com/v1/chat/completions";
const OPEN_AI_MODEL = "gpt-4o-mini";

// preparation
export function prepareArticles(items: Article[]) {
  return items.map((item: Article) => ({
    id: item.Id,
    name: item.Name,
  }));
}

// batching
export function createBatches(preparedArticles: any[], batchSize: number) {
  const batches = [];
  for (let i = 0; i < preparedArticles.length; i += batchSize) {
    batches.push(preparedArticles.slice(i, i + batchSize));
  }
  return batches;
}

// Utility-Funktion für Verzögerung
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

// Batching-Funktionen auslagern
export async function processBatch(
  batch: any[],
  promptGenerator: (batch: any[]) => string,
  callApi: (prompt: string) => Promise<AxiosResponse<any, any>>,
  handleApiResponse: (response: AxiosResponse<any, any>) => any[],
  maxRetries = 2,
  retryDelay = 2000,
  attempt = 1
): Promise<any[]> {
  const prompt = promptGenerator(batch);
  try {
    const response = await callApi(prompt);
    const parsedData = handleApiResponse(response);
    return parsedData;
  } catch (error) {
    if (attempt < maxRetries) {
      console.warn(
        `Fehler bei der Batch-Verarbeitung (Versuch ${attempt} von ${maxRetries}). Erneuter Versuch in ${retryDelay}ms...`,
        error
      );
      await delay(retryDelay);
      return processBatch(batch, promptGenerator, callApi, handleApiResponse, maxRetries, retryDelay, attempt + 1);
    } else {
      console.error("Maximale Anzahl von Versuchen erreicht.", error);
      throw error;
    }
  }
}

export async function processAllBatches(
  batches: any[],
  promptGenerator: (batch: any[]) => string,
  handleBatchResponse: (data: any[]) => Promise<void>,
  concurrencyLimit = 5
) {
  let index = 0;

  const executeBatches = async () => {
    const promises = [];
    while (index < batches.length && promises.length < concurrencyLimit) {
      const batch = batches[index++];
      const promise = processBatch(
        batch,
        promptGenerator,
        callOpenAiApi,
        handleResponse
      ).then(async (parsedData) => {
        await handleBatchResponse(parsedData);
      });
      promises.push(promise);
    }
    await Promise.all(promises);
    if (index < batches.length) {
      await executeBatches();
    }
  };

  await executeBatches();
}

// call
async function callOpenAiApi(prompt: string) {
  return axios.post(
    OPEN_AI_URL,
    {
      model: OPEN_AI_MODEL,
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
}

// receiving / parsing
function handleResponse(response: AxiosResponse<any, any>) {
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
}
