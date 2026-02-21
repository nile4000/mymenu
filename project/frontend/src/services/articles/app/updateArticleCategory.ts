import { Article } from "src/helpers/interfaces/article.interface";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { upsertArticleCategory, upsertArticleCategories } from "../infra/articleSupabaseRepo";

export async function updateArticleCategory(article: Article): Promise<ServiceResult<unknown>> {
  if (!article.Id || !article.Category) {
    return err(toServiceError("VALIDATION_ERROR", "Artikel-ID und Kategorie sind erforderlich."));
  }
  try {
    return ok(await upsertArticleCategory({ id: article.Id, category: article.Category }));
  } catch (cause) {
    return err(toServiceError("ARTICLE_UPDATE_CATEGORY_ERROR", "Kategorie konnte nicht aktualisiert werden.", cause));
  }
}

export async function updateArticleCategories(
  payload: { id: string; category: string }[]
): Promise<ServiceResult<unknown>> {
  if (!payload.length) return ok(undefined);
  try {
    return ok(await upsertArticleCategories(payload));
  } catch (cause) {
    return err(
      toServiceError("ARTICLE_UPDATE_CATEGORIES_ERROR", "Kategorien konnten nicht aktualisiert werden.", cause)
    );
  }
}
