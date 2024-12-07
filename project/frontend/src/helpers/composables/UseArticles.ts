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

export function useArticles($q: any) {
  const rows = reactive<Article[]>([]);
  const receipts = reactive<Record<string, Receipt>>({});
  let channel: any;

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
    const eventType = payload.eventType;
    switch (eventType) {
      case "INSERT":
        rows.push(newArticle);
        break;
      case "DELETE":
        {
          const indexToDelete = rows.findIndex(
            (article) => article.Id === newArticle.Id
          );
          if (indexToDelete !== -1) rows.splice(indexToDelete, 1);
        }
        break;
      case "UPDATE":
        {
          const updateIndex = rows.findIndex(
            (article) => article.Id === newArticle.Id
          );
          if (updateIndex !== -1) {
            rows[updateIndex] = newArticle;
          } else {
            rows.push(newArticle);
          }
        }
        break;
      default:
        // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
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
