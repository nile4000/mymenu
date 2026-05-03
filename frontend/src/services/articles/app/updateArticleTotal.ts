import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { upsertArticleTotal } from "../infra/articleSupabaseRepo";

export async function updateArticleTotal(id: string, total?: number): Promise<ServiceResult<unknown>> {
  if (!id || total === undefined || total === null || Number.isNaN(total)) {
    return err(toServiceError("VALIDATION_ERROR", "Artikel-ID und gültiger Total-Wert sind erforderlich."));
  }
  try {
    return ok(await upsertArticleTotal({ id, total }));
  } catch (cause) {
    return err(toServiceError("ARTICLE_UPDATE_TOTAL_ERROR", "Total konnte nicht aktualisiert werden.", cause));
  }
}
