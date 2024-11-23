import { Article } from "./article.interface";

export interface Receipt {
  Id?: string;
  Uuid?: string;
  Total_Receipt: number;
  Total_Calculated?: number;
  PurchaseDate: string;
  Articles?: Article[];
  Corp: string;
  Total_R_Open_Ai?: number;
  Total_R_Extract?: number;
}
