import { Article } from "./article.interface";
import { Receipt } from "./receipt.interface";

export interface ExtractArticleRaw {
  id?: string | number;
  name: string;
  quantity: number | string;
  price: number | string;
  total: number | string;
  discount: number | string;
  category: string;
}

export interface ExtractMetadataRaw {
  extractedTotalRow: number;
  openAiArticleCount: number;
}

export interface ExtractResponseRaw {
  uid: string;
  purchaseDate: string;
  corp: string;
  total: number | string;
  articles: ExtractArticleRaw[];
  metadata?: ExtractMetadataRaw;
}

export interface NormalizedExtractResponse {
  articles: Article[];
  receipt: Receipt;
}
