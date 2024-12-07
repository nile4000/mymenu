import { reactive } from "vue";
import { Article } from "../interfaces/article.interface";
import { Receipt } from "../interfaces/receipt.interface";
import {
  readAllArticles,
  readReceiptsByIds,
} from "src/services/readAllArticles";
import {
  subscribeToArticleChanges,
  unsubscribeFromArticleChanges,
} from "src/services/realtimeArticles";
import {
  upsertArticleCategory,
  upsertArticleUnit,
} from "src/services/updateArticle";
import { QVueGlobals } from "quasar";
import { RealtimeChannel } from "@supabase/supabase-js";

export function useArticles($q: QVueGlobals) {
  const rows = reactive<any>([]);
  const receipts = reactive<Record<string, Receipt>>({});
  let channel: RealtimeChannel;

  async function loadArticles() {
    const data = await readAllArticles();
    if (data) {
      rows.splice(0, rows.length, ...data);
      await fetchReceiptsForArticles(data);
    }
  }

  async function fetchReceiptsForArticles(articles: Article[]) {
    const uniqueReceiptIds = [
      ...new Set(
        articles.map((a) => a.Receipt_Id).filter((id): id is string => !!id)
      ),
    ];
    const idsToFetch = uniqueReceiptIds.filter((id) => !receipts[id]);
    if (idsToFetch.length > 0) {
      const fetchedReceipts = await readReceiptsByIds(idsToFetch);
      fetchedReceipts.forEach((receipt) => {
        if (receipt.Id) {
          receipts[receipt.Id] = receipt;
        }
      });
    }
  }

  function handleArticleChange(payload: any) {
    const newArticle = payload.new as Article;
    const oldArticle = payload.old as Article;
    const eventType = payload.eventType;
    switch (eventType) {
      case "INSERT":
        rows.push(newArticle);
        break;
      case "DELETE":
        {
          const indexToDelete = rows.findIndex(
            (article: Article) => article.Id === oldArticle.Id
          );
          if (indexToDelete !== -1) rows.splice(indexToDelete, 1);
        }
        break;
      case "UPDATE":
        {
          const updateIndex = rows.findIndex(
            (article: Article) => article.Id === newArticle.Id
          );
          if (updateIndex !== -1) {
            rows[updateIndex] = newArticle;
          } else {
            rows.push(newArticle);
          }
        }
        break;
      default:
        $q.notify({
          type: "warning",
          message: `Unknown event type: ${eventType}`,
        });
    }
  }

  function subscribeToArticles() {
    channel = subscribeToArticleChanges(handleArticleChange);
  }

  function unsubscribeArticles() {
    if (channel) {
      unsubscribeFromArticleChanges(channel);
    }
  }

  async function updateCategory(article: Article) {
    await upsertArticleCategory(article);
  }

  async function updateUnit(article: Article) {
    await upsertArticleUnit(article.Id, article.Unit);
  }

  return {
    rows,
    receipts,
    loadArticles,
    subscribeToArticles,
    unsubscribeArticles,
    updateCategory,
    updateUnit,
  };
}
