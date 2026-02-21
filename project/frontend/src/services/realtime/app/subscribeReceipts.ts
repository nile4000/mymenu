import { RealtimeChannel } from "@supabase/supabase-js";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { RealtimeChangeCallback, subscribeToTable, unsubscribeChannel } from "../infra/realtimeSupabaseRepo";

export function subscribeReceipts(callback: RealtimeChangeCallback<any>): ServiceResult<RealtimeChannel> {
  try {
    return ok(subscribeToTable("receipt-changes", "receipt", callback));
  } catch (cause) {
    return err(
      toServiceError(
        "REALTIME_SUBSCRIBE_RECEIPT_ERROR",
        "Realtime-Channel fuer Kassenzettel konnte nicht erstellt werden.",
        cause
      )
    );
  }
}

export function unsubscribeRealtime(channel: RealtimeChannel): ServiceResult<void> {
  try {
    unsubscribeChannel(channel);
    return ok(undefined);
  } catch (cause) {
    return err(
      toServiceError("REALTIME_UNSUBSCRIBE_ERROR", "Realtime-Channel konnte nicht geschlossen werden.", cause)
    );
  }
}
