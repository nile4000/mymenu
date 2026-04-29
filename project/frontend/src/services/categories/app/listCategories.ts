import { CategoryDto } from "../api/categoryContracts";
import { http } from "src/services/shared/infra/http";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";

export async function listCategories(): Promise<ServiceResult<CategoryDto[]>> {
  try {
    const response = await http.get<CategoryDto[]>("/api/categories");
    return ok(response.data);
  } catch (cause) {
    return err(toServiceError("CATEGORIES_FETCH_ERROR", "Kategorien laden fehlgeschlagen.", cause));
  }
}
