import { RealtimeChannel } from "@supabase/supabase-js";
import { supabase } from "src/boot/supabase";

export type RealtimeChangePayload<T> = {
  eventType: "INSERT" | "UPDATE" | "DELETE";
  new: T;
  old: T;
};

export type RealtimeChangeCallback<T> = (payload: RealtimeChangePayload<T>) => void;

export function subscribeToTableChanges<T>(
  channelName: string,
  table: string,
  callback: RealtimeChangeCallback<T>
): RealtimeChannel {
  return supabase
    .channel(channelName)
    .on("postgres_changes", { event: "*", schema: "public", table }, (payload) => {
      callback(payload as unknown as RealtimeChangePayload<T>);
    })
    .subscribe();
}

export function unsubscribeFromChanges(channel: RealtimeChannel): void {
  void supabase.removeChannel(channel);
}
