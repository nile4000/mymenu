import { Article } from "src/helpers/interfaces/article.interface";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { fetchAllArticles } from "../infra/articleSupabaseRepo";

export async function listArticles(): Promise<ServiceResult<Article[]>> {
  try {
    return ok((await fetchAllArticles()) ?? []);
  } catch (cause) {
    return err(toServiceError("ARTICLE_LIST_ERROR", "Artikel konnten nicht geladen werden.", cause));
  }
}
