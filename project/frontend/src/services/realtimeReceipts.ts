import { supabase } from "src/boot/supabase";

type ReceiptChangeCallback = (payload: any) => void;

/**
 * Setup of the realtime channel for article changes
 */
export function subscribeToReceiptChanges(callback: ReceiptChangeCallback) {
  const channel = supabase
    .channel("custom-all-channel")
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
export function unsubscribeFromArticleChanges(channel: any) {
  void supabase.removeChannel(channel);
}
