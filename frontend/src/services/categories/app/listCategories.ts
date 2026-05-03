import { Category } from "../api/categoryContracts";
import { http } from "src/services/shared/infra/http";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";

export async function listCategories(): Promise<ServiceResult<Category[]>> {
  try {
    const response = await http.get<Category[]>("/api/categories");
    return ok(response.data);
  } catch (cause) {
    return err(toServiceError("CATEGORIES_FETCH_ERROR", "Kategorien laden fehlgeschlagen.", cause));
  }
}
