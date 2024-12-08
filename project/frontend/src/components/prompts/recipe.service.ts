import { AxiosResponse } from "axios";
import { Article } from "../../helpers/interfaces/article.interface";
import { callOpenAiApi } from "../../services/aiRequest.service";

// define structure
const RECIPE_STRUCTURE = {
  title: "string",
  description: "string",
  cookingTime: "string",
  category: "string",
  servings: "number",
  color: "string",
  ingredients: ["string"],
  stepsList: ["string"],
  image: "string",
};

const systemPrompt = `You are a cooking-recipe assistant. You will receive a list of ingredients and should return a single JSON without explanation, in this exact structure: ${JSON.stringify(
  RECIPE_STRUCTURE
)} Use only the given ingredients. Make sure the JSON is valid and does not contain any additional text. If you cannot comply, return an empty JSON {}.`;

function createExampleRecipeJson(servings: number): string {
  // set placeholder values of structure
  const example = {
    title: "Titel des Rezepts",
    description: "Kurze Beschreibung des Gerichts",
    cookingTime: "45 Min",
    category: "Kategorie des Gerichts",
    servings: servings,
    color: "card-background1",
    ingredients: ["Zutat 1", "Zutat 2"],
    stepsList: ["Schritt 1", "Schritt 2"],
    image: "https://example.com/image.jpg",
  };

  return JSON.stringify(example, null, 2);
}

export function createRecipePrompt(
  articles: Article[],
  servings: number
): string {
  const ingredientsList = articles
    .map(
      (a, index) =>
        `${index + 1}. ${a.Name}, Menge: ${
          a.Quantity || "unbekannt"
        }, Einheit: ${a.Unit || "keine Einheit"}`
    )
    .join("\n");

  const exampleJson = createExampleRecipeJson(servings);

  return `Hier sind meine verfügbaren Zutaten:
        ${ingredientsList}
        Erstelle ein Rezept für ca. ${servings} Personen, das diese Zutaten verwendet. Gib nur ein gültiges JSON im folgenden Format zurück, ohne weitere Erklärungen:
        ${exampleJson}
        Bitte mache keine anderen Ausgaben, nur dieses JSON.`;
}

function parseSingleRecipeResponse(response: AxiosResponse<any, any>): any {
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

  const resultText = response.data.choices[0].message.content.trim();
  if (!resultText) {
    throw new Error("Keine Antwort vom Modell erhalten.");
  }

  let recipe;
  try {
    recipe = JSON.parse(resultText);
  } catch (err) {
    throw new Error("Die Antwort ist kein valides JSON.");
  }

  if (!recipe || !recipe.title) {
    throw new Error("Kein gültiges Rezept gefunden.");
  }

  return recipe;
}

export async function sendSingleRecipeRequest(
  articles: Article[],
  servings: number
): Promise<any> {
  const prompt = createRecipePrompt(articles, servings);

  const response = await callOpenAiApi(
    prompt,
    "gpt-4o-mini-2024-07-18",
    systemPrompt
  );

  return parseSingleRecipeResponse(response);
}
