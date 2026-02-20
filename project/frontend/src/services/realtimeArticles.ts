import { RealtimeChannel } from "@supabase/supabase-js";
import {
  RealtimeChangeCallback,
  subscribeToTableChanges,
  unsubscribeFromChanges,
} from "./realtime";

type ArticleChangeCallback = RealtimeChangeCallback<any>;

/**
 * Setup of the realtime channel for article changes
 */
export function subscribeToArticleChanges(callback: ArticleChangeCallback) {
  return subscribeToTableChanges("article-changes", "article", callback);
}

/**
 * Closes the realtime channel
 */
export function unsubscribeFromArticleChanges(channel: RealtimeChannel) {
  unsubscribeFromChanges(channel);
}
