import { supabase } from "src/boot/supabase";
import { RealtimeChannel } from "@supabase/supabase-js";

type ReceiptChangeCallback = (payload: any) => void;

/**
 * Setup of the realtime channel for receipt changes
 */
export function subscribeToReceiptChanges(callback: ReceiptChangeCallback) {
  const channel = supabase
    .channel("receipt-changes")
    .on(
      "postgres_changes",
      { event: "*", schema: "public", table: "receipt" },
      (payload) => {
        callback(payload);
      }
    )
    .subscribe();

  return channel;
}

/**
 * Closes the realtime channel
 */
export function unsubscribeFromReceiptChanges(channel: RealtimeChannel) {
  void supabase.removeChannel(channel);
}
