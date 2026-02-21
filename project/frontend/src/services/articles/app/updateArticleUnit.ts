import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { upsertArticleUnit, upsertArticleUnits } from "../infra/articleSupabaseRepo";

export async function updateArticleUnit(id: string, unit?: string): Promise<ServiceResult<unknown>> {
  if (!id || !unit) {
    return err(toServiceError("VALIDATION_ERROR", "Artikel-ID und Einheit sind erforderlich."));
  }
  try {
    return ok(await upsertArticleUnit({ id, unit }));
  } catch (cause) {
    return err(toServiceError("ARTICLE_UPDATE_UNIT_ERROR", "Einheit konnte nicht aktualisiert werden.", cause));
  }
}

export async function updateArticleUnits(payload: { id: string; unit: string }[]): Promise<ServiceResult<unknown>> {
  if (!payload.length) return ok(undefined);
  try {
    return ok(await upsertArticleUnits(payload));
  } catch (cause) {
    return err(toServiceError("ARTICLE_UPDATE_UNITS_ERROR", "Einheiten konnten nicht aktualisiert werden.", cause));
  }
}
