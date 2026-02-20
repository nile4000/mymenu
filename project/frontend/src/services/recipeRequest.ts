import { AxiosResponse } from "axios";
import { Article } from "../helpers/interfaces/article.interface";
import {
  Recipe,
  RecipeIngredient,
} from "../helpers/interfaces/recipe.interface";
import { callOpenAiApi } from "./aiRequest";

const RECIPE_SCHEMA_DESCRIPTION =
  "{ title: string, description: string, cookingTime: string, category: string, servings: number, color: string, ingredients: [{ name: string, amount: number, unit: string }], stepsList: string[], image: string }";

const systemPrompt = `You are a cooking-recipe assistant. You will receive a list of ingredients and should return a single JSON without explanation, in this exact structure: ${RECIPE_SCHEMA_DESCRIPTION}
Return ONLY one valid JSON object.
No markdown, no code fences, no comments, no extra text.
Use only edible ingredients from the user list and ignore non-food items.
"servings" must be a number and must match the requested servings exactly.
Each ingredient must include name (string), amount (number), and unit (string, non-empty).
Use realistic ingredient amounts and units for the requested servings.
"stepsList" must be a non-empty string array.
If you cannot comply exactly, return {}.`;

function createExampleRecipeJson(servings: number): string {
  const example = {
    title: "Titel des Rezepts",
    description: "Kurze Beschreibung des Gerichts",
    cookingTime: "45 Min",
    category: "Kategorie des Gerichts",
    servings,
    color: "card-background1",
    ingredients: [
      { name: "Brokkoli", amount: 400, unit: "g" },
      { name: "Reis", amount: 250, unit: "g" },
      { name: "Olivenoel", amount: 2, unit: "EL" },
    ],
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

  return `Hier sind meine verfuegbaren Zutaten:\n${ingredientsList}\nErstelle ein gutes Rezept fuer ca. ${servings} Personen, das diese Zutaten verwendet. Verwende nur Zutaten aus dieser Liste und fuege keine weiteren hinzu. Setze "servings" exakt auf ${servings} und skaliere die Mengen passend fuer ${servings} Personen. Gib nur ein gueltiges JSON im folgenden Format zurueck, ohne weitere Erklaerungen:\n${exampleJson}\nBitte mache keine anderen Ausgaben, nur dieses JSON.`;
}

function normalizeIngredient(value: unknown): RecipeIngredient | null {
  if (!value || typeof value !== "object") {
    return null;
  }

  const ingredient = value as Partial<RecipeIngredient>;
  if (
    !ingredient.name ||
    typeof ingredient.name !== "string" ||
    typeof ingredient.amount !== "number" ||
    !Number.isFinite(ingredient.amount) ||
    typeof ingredient.unit !== "string" ||
    ingredient.unit.trim().length === 0
  ) {
    return null;
  }

  return {
    name: ingredient.name,
    amount: ingredient.amount,
    unit: ingredient.unit.trim(),
  };
}

function parseSingleRecipeResponse(response: AxiosResponse<any, any>): Recipe {
  if (
    !response.data ||
    !response.data.choices ||
    !Array.isArray(response.data.choices) ||
    response.data.choices.length === 0 ||
    !response.data.choices[0].message ||
    !response.data.choices[0].message.content
  ) {
    throw new Error("Ungueltige API-Antwortstruktur.");
  }

  const resultText = response.data.choices[0].message.content
    .replace(/```json|```/g, "")
    .trim();

  if (!resultText) {
    throw new Error("Keine Antwort vom Modell erhalten.");
  }

  let recipe: Partial<Recipe>;
  try {
    recipe = JSON.parse(resultText);
  } catch {
    throw new Error("Die Antwort ist kein valides JSON.");
  }

  const normalizedIngredients = Array.isArray(recipe.ingredients)
    ? recipe.ingredients
        .map((value) => normalizeIngredient(value))
        .filter((value): value is RecipeIngredient => Boolean(value))
    : [];

  if (
    !recipe ||
    !recipe.title ||
    !Array.isArray(recipe.stepsList) ||
    normalizedIngredients.length === 0
  ) {
    throw new Error("Kein gueltiges Rezept gefunden.");
  }

  return {
    title: recipe.title,
    description: recipe.description || "",
    cookingTime: recipe.cookingTime || "",
    category: recipe.category || "",
    servings: Number(recipe.servings) || 2,
    color: recipe.color || "card-background1",
    ingredients: normalizedIngredients,
    stepsList: recipe.stepsList,
    image: recipe.image || "",
  };
}

export async function sendSingleRecipeRequest(
  articles: Article[],
  servings: number
): Promise<Recipe> {
  const prompt = createRecipePrompt(articles, servings);

  const response = await callOpenAiApi(
    prompt,
    "gpt-4o-mini-2024-07-18",
    systemPrompt
  );

  const recipe = parseSingleRecipeResponse(response);
  return {
    ...recipe,
    servings,
  };
}
