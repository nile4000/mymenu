import { defineStore } from "pinia";
import { RealtimeChannel } from "@supabase/supabase-js";
import { Article } from "src/helpers/interfaces/article.interface";
import { Receipt } from "src/helpers/interfaces/receipt.interface";
import { readAllArticles } from "src/services/readArticles";
import { readAllReceipts } from "src/services/readReceipts";
import {
  subscribeToArticleChanges,
} from "src/services/realtimeArticles";
import {
  subscribeToReceiptChanges,
} from "src/services/realtimeReceipts";

function upsertById<T extends { Id?: string }>(items: T[], next: T): T[] {
  if (!next.Id) {
    return items;
  }
  const index = items.findIndex((item) => item.Id === next.Id);
  if (index === -1) {
    return [...items, next];
  }
  const updated = [...items];
  updated[index] = next;
  return updated;
}

export const useDataStore = defineStore("data", {
  state: () => ({
    articles: [] as Article[],
    receipts: [] as Receipt[],
    initialized: false,
    initializing: false,
    articleChannel: null as RealtimeChannel | null,
    receiptChannel: null as RealtimeChannel | null,
  }),
  getters: {
    receiptById(state): Record<string, Receipt> {
      return state.receipts.reduce((acc, receipt) => {
        if (receipt.Id) {
          acc[receipt.Id] = receipt;
        }
        return acc;
      }, {} as Record<string, Receipt>);
    },
  },
  actions: {
    async ensureInitialized() {
      if (this.initialized || this.initializing) {
        return;
      }

      this.initializing = true;
      try {
        const [articles, receipts] = await Promise.all([
          readAllArticles(),
          readAllReceipts(),
        ]);
        this.articles = articles ?? [];
        this.receipts = receipts ?? [];
        this.initialized = true;
      } finally {
        this.initializing = false;
      }
    },
    startRealtime() {
      if (!this.articleChannel) {
        this.articleChannel = subscribeToArticleChanges((payload: any) => {
          const newArticle = payload.new as Article;
          const oldArticle = payload.old as Article;

          switch (payload.eventType) {
            case "INSERT":
            case "UPDATE":
              this.articles = upsertById(this.articles, newArticle);
              break;
            case "DELETE":
              this.articles = this.articles.filter(
                (article) => article.Id !== oldArticle.Id
              );
              break;
            default:
              break;
          }
        });
      }

      if (!this.receiptChannel) {
        this.receiptChannel = subscribeToReceiptChanges((payload: any) => {
          const newReceipt = payload.new as Receipt;
          const oldReceipt = payload.old as Receipt;

          switch (payload.eventType) {
            case "INSERT":
            case "UPDATE":
              this.receipts = upsertById(this.receipts, newReceipt);
              break;
            case "DELETE":
              this.receipts = this.receipts.filter(
                (receipt) => receipt.Id !== oldReceipt.Id
              );
              break;
            default:
              break;
          }
        });
      }
    },
    stopRealtime() {
      if (this.articleChannel) {
        void this.articleChannel.unsubscribe();
        this.articleChannel = null;
      }
      if (this.receiptChannel) {
        void this.receiptChannel.unsubscribe();
        this.receiptChannel = null;
      }
    },
    removeReceiptById(id: string) {
      this.receipts = this.receipts.filter((receipt) => receipt.Id !== id);
    },
  },
});
