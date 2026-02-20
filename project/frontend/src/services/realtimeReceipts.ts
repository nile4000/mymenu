import { RealtimeChannel } from "@supabase/supabase-js";
import {
  RealtimeChangeCallback,
  subscribeToTableChanges,
  unsubscribeFromChanges,
} from "./realtime";

type ReceiptChangeCallback = RealtimeChangeCallback<any>;

/**
 * Setup of the realtime channel for receipt changes
 */
export function subscribeToReceiptChanges(callback: ReceiptChangeCallback) {
  return subscribeToTableChanges("receipt-changes", "receipt", callback);
}

/**
 * Closes the realtime channel
 */
export function unsubscribeFromReceiptChanges(channel: RealtimeChannel) {
  unsubscribeFromChanges(channel);
}
