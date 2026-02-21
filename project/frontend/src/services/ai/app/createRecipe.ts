import { Article } from "src/helpers/interfaces/article.interface";
import { Recipe } from "src/helpers/interfaces/recipe.interface";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { postRecipe } from "../infra/aiHttpClient";

export async function createRecipe(articles: Article[], servings: number): Promise<ServiceResult<Recipe>> {
  try {
    const items = articles.map((article) => ({
      name: article.Name,
      quantity: Number(article.Quantity ?? 0),
      unit: article.Unit || "stk",
    }));

    const response = await postRecipe(items, servings);
    return ok(response.data);
  } catch (cause) {
    return err(toServiceError("AI_RECIPE_ERROR", "Rezept konnte nicht erstellt werden.", cause));
  }
}
