import { Article } from "./article.interface";

export interface Receipt {
  Total: number;
  PurchaseDate: string;
  Articles: Article[];
  Corp: string;
}
