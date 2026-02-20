import { Article } from "src/helpers/interfaces/article.interface";
import {
  ExtractArticleRaw,
  ExtractResponseRaw,
  NormalizedExtractResponse,
} from "src/helpers/interfaces/extract-response.interface";
import { Receipt } from "src/helpers/interfaces/receipt.interface";

function toNumber(value: unknown, fallback = 0): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function normalizeArticle(article: ExtractArticleRaw): Article {
  const price = toNumber(article.price, 0);
  const quantity = toNumber(article.quantity, 0);
  const total = toNumber(article.total, price);

  return {
    Id: String(article.id ?? ""),
    Name: String(article.name ?? ""),
    Quantity: quantity,
    Price: price,
    Total: total,
    Discount: toNumber(article.discount, 0),
    Category: String(article.category ?? ""),
    Unit: undefined,
    Purchase_Date: "",
  };
}

function normalizeReceipt(raw: ExtractResponseRaw): Receipt {
  const nowIso = new Date().toISOString();

  return {
    Uuid: raw.uid,
    Purchase_Date: raw.purchaseDate ?? "",
    Created_At: nowIso,
    Corp: raw.corp ?? "Unknown",
    Total_R_Extract: toNumber(raw.metadata?.extractedTotalRow, 0),
    Total_R_Open_Ai: toNumber(raw.metadata?.openAiArticleCount, 0),
    Total_Receipt: toNumber(raw.total, 0),
  };
}

export function normalizeExtractResponse(
  raw: ExtractResponseRaw
): NormalizedExtractResponse {
  const articles = raw.articles.map(normalizeArticle);

  return {
    articles,
    receipt: normalizeReceipt(raw),
  };
}
