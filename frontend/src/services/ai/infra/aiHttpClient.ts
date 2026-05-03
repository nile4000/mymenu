import { http } from "src/services/shared/infra/http";
import {
  CategorizeResultItem,
  CategorizeItem,
  ExtractUnitResultItem,
  ExtractUnitItem,
  RecipeItem,
  RecipeResponse,
} from "../api/aiContracts";

export function postAiTask(task: "categorize", items: CategorizeItem[]) {
  return http.post<CategorizeResultItem[]>(`/api/ai/${task}`, { items });
}

export function postExtractUnitTask(task: "extract-unit", items: ExtractUnitItem[]) {
  return http.post<ExtractUnitResultItem[]>(`/api/ai/${task}`, { items });
}

export function postRecipe(items: RecipeItem[], servings: number) {
  return http.post<RecipeResponse>("/api/ai/recipe", { items, servings });
}
