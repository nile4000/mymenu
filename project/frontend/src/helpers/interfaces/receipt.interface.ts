import { Article } from "./article.interface";

export interface Receipt {
  Total: number;
  Purchase_Date: string;
  Articles: Article[];
  Corp: string;
}
