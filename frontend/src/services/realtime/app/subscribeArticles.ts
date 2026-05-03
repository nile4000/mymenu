import { RealtimeChannel } from "@supabase/supabase-js";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { RealtimeChangeCallback, subscribeToTable } from "../infra/realtimeSupabaseRepo";

export function subscribeArticles(callback: RealtimeChangeCallback<any>): ServiceResult<RealtimeChannel> {
  try {
    return ok(subscribeToTable("article-changes", "article", callback));
  } catch (cause) {
    return err(
      toServiceError(
        "REALTIME_SUBSCRIBE_ARTICLE_ERROR",
        "Realtime-Channel fuer Artikel konnte nicht erstellt werden.",
        cause
      )
    );
  }
}
