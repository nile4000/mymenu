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

function firstDefined<T>(...values: (T | undefined)[]): T | undefined {
  return values.find((value) => value !== undefined);
}

function normalizeArticle(article: ExtractArticleRaw): Article {
  const price = toNumber(firstDefined(article.price, article.Price), 0);
  const quantity = toNumber(firstDefined(article.quantity, article.Quantity), 0);
  const total = toNumber(firstDefined(article.total, article.Total), price);

  return {
    Id: String(article.id ?? ""),
    Name: String(firstDefined(article.name, article.Name) ?? ""),
    Quantity: quantity,
    Price: price,
    Total: total,
    Discount: toNumber(firstDefined(article.discount, article.Discount), 0),
    Category: String(firstDefined(article.category, article.Category) ?? ""),
    Unit: undefined,
    Purchase_Date: "",
  };
}

function normalizeReceipt(raw: ExtractResponseRaw): Receipt {
  const nowIso = new Date().toISOString();
  const metadata = firstDefined(raw.metadata, raw.Metadata);

  return {
    Uuid: String(firstDefined(raw.uid, raw.UID) ?? ""),
    Purchase_Date: String(firstDefined(raw.purchaseDate, raw.PurchaseDate) ?? ""),
    Created_At: nowIso,
    Corp: String(firstDefined(raw.corp, raw.Corp) ?? "Unknown"),
    Total_R_Extract: toNumber(metadata?.extractedTotalRow, 0),
    Total_R_Open_Ai: toNumber(metadata?.openAiArticleCount, 0),
    Total_Receipt: toNumber(firstDefined(raw.total, raw.Total), 0),
  };
}

export function parseExtractResponse(raw: ExtractResponseRaw): NormalizedExtractResponse {
  const rawArticles = firstDefined(raw.articles, raw.Articles);
  const articles = Array.isArray(rawArticles) ? rawArticles.map(normalizeArticle) : [];
  return {
    articles,
    receipt: normalizeReceipt(raw),
  };
}
