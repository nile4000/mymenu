import { supabase } from "src/boot/supabase";

type ArticleChangeCallback = (payload: any) => void;

/**
 * Setup of the realtime channel for article changes
 */
export function subscribeToArticleChanges(callback: ArticleChangeCallback) {
  const channel = supabase
    .channel("custom-all-channel")
    .on(
      "postgres_changes",
      { event: "*", schema: "public", table: "article" },
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
