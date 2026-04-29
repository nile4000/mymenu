import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { removeArticleById, removeArticlesByIds } from "../infra/articleSupabaseRepo";

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

export async function deleteArticles(ids: string[]): Promise<ServiceResult<unknown>> {
  const valid = ids.filter(Boolean);
  if (!valid.length) {
    return err(toServiceError("VALIDATION_ERROR", "Mindestens eine Artikel-ID ist erforderlich."));
  }
  try {
    return ok(await removeArticlesByIds(valid));
  } catch (cause) {
    return err(toServiceError("ARTICLE_DELETE_ERROR", "Artikel konnten nicht geloescht werden.", cause));
  }
}
