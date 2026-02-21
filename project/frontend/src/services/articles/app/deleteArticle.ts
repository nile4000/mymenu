import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { removeArticleById } from "../infra/articleSupabaseRepo";

export async function deleteArticle(id: string): Promise<ServiceResult<unknown>> {
  if (!id) {
    return err(toServiceError("VALIDATION_ERROR", "Artikel-ID ist erforderlich."));
  }
  try {
    return ok(await removeArticleById(id));
  } catch (cause) {
    return err(toServiceError("ARTICLE_DELETE_ERROR", "Artikel konnte nicht geloescht werden.", cause));
  }
}
