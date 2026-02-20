import axios, { AxiosResponse } from "axios";
import { Ref } from "vue";

const OPEN_AI_URL = "https://api.openai.com/v1/chat/completions";
const OPEN_AI_TEMP_MODEL = "gpt-3.5-turbo-0125";

type PreparedPromptItem = {
  id: string;
  name: string;
  quantity?: number;
  price?: number;
};

type ArticleNameRef = {
  Id: string;
  Name: string;
};

type ArticleDetailRef = {
  Id: string;
  Name: string;
  Quantity: number;
  Total?: number;
  Price?: number;
};

type OpenAiChatResponse = {
  choices?: Array<{
    message?: {
      content?: string;
    };
  }>;
};

// preparation
export function prepareArticles(items: ArticleNameRef[]) {
  return items.map((item: ArticleNameRef) => ({
    id: item.Id,
    name: item.Name,
  }));
}

export function prepareArticlesPrices(items: ArticleDetailRef[]) {
  return items.map((item: ArticleDetailRef) => ({
    id: item.Id,
    name: item.Name,
    quantity: item.Quantity,
    price: item.Total ?? item.Price ?? 0,
  }));
}

export function prepareDialogArticles(items: ArticleNameRef[]) {
  return items.map((item: ArticleNameRef) => ({
    id: item.Id,
    name: item.Name,
  }));
}

// batching
export function createBatches<T>(preparedArticles: T[], batchSize: number) {
  const batches: T[][] = [];
  for (let i = 0; i < preparedArticles.length; i += batchSize) {
    batches.push(preparedArticles.slice(i, i + batchSize));
  }
  return batches;
}

// Utility-Funktion
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export async function processBatch(
  batch: PreparedPromptItem[],
  promptGenerator: (batch: PreparedPromptItem[]) => string,
  callApi: (
    prompt: string,
    model: string,
    systemPrompt: string
  ) => Promise<AxiosResponse<OpenAiChatResponse>>,
  handleApiResponse: (response: AxiosResponse<OpenAiChatResponse>) => unknown[],
  model: string,
  systemPrompt: Ref<string>,
  maxRetries = 2,
  retryDelay = 2000,
  attempt = 1
): Promise<unknown[]> {
  const prompt = promptGenerator(batch);
  try {
    const response = await callApi(prompt, model, systemPrompt.value);
    const parsedData = handleApiResponse(response);
    return parsedData;
  } catch (error) {
    if (attempt < maxRetries) {
      console.warn(
        `Error processing batch (Attempt ${attempt} of ${maxRetries}). Retrying in ${retryDelay}ms...`,
        error
      );
      await delay(retryDelay);
      return processBatch(
        batch,
        promptGenerator,
        callApi,
        handleApiResponse,
        model,
        systemPrompt,
        maxRetries,
        retryDelay,
        attempt + 1
      );
    } else {
      console.error("Maximum retry attempts reached.", error);
      throw error;
    }
  }
}

export async function processAllBatches(
  batches: PreparedPromptItem[][],
  promptGenerator: (batch: PreparedPromptItem[]) => string,
  handleBatchResponse: (data: unknown[]) => Promise<void>,
  model: string,
  systemPrompt: Ref<string>
) {
  let index = 0;

  const executeBatches = async () => {
    const promises = [];
    const concurrencyLimit = 5;
    while (index < batches.length && promises.length < concurrencyLimit) {
      const batch = batches[index++];
      const promise = processBatch(
        batch,
        promptGenerator,
        callOpenAiApi,
        handleResponse,
        model,
        systemPrompt
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

// get request for open ai
export async function callOpenAiApi(
  prompt: string,
  model: string,
  systemPrompt: string
) {
  return axios.post(
    OPEN_AI_URL,
    {
      model: model || OPEN_AI_TEMP_MODEL,
      messages: [
        {
          role: "system",
          content: systemPrompt,
        },
        {
          role: "user",
          content: prompt,
        },
      ],
      temperature: 0, // works deterministic with 0
    },
    {
      headers: {
        Authorization: `Bearer ${process.env.OPENAI_API_KEY}`,
        "Content-Type": "application/json",
      },
      timeout: 30000,
    }
  );
}

// receiving / parsing
function handleResponse(response: AxiosResponse<OpenAiChatResponse>) {
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

  let parsedData: unknown[];
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
