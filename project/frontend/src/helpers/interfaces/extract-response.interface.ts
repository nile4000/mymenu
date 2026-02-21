import { Article } from "./article.interface";
import { Receipt } from "./receipt.interface";

export interface ExtractArticleRaw {
  id?: string | number;
  name?: string;
  Name?: string;
  quantity?: number | string;
  Quantity?: number | string;
  price?: number | string;
  Price?: number | string;
  total?: number | string;
  Total?: number | string;
  discount?: number | string;
  Discount?: number | string;
  category?: string;
  Category?: string;
}

export interface ExtractMetadataRaw {
  extractedTotalRow: number;
  openAiArticleCount: number;
}

export interface ExtractResponseRaw {
  uid?: string;
  UID?: string;
  purchaseDate?: string;
  PurchaseDate?: string;
  corp?: string;
  Corp?: string;
  total?: number | string;
  Total?: number | string;
  articles?: ExtractArticleRaw[];
  Articles?: ExtractArticleRaw[];
  metadata?: ExtractMetadataRaw;
  Metadata?: ExtractMetadataRaw;
}

export interface NormalizedExtractResponse {
  articles: Article[];
  receipt: Receipt;
}
