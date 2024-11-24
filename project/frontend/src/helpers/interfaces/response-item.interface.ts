import { Article } from "./article.interface";

export interface ResponseItem {
  UID: string;
  Articles: Article[];
  Purchase_Date: string;
  Created_At: string;
  Corp: string;
  Total: string;
  Total_R_Extract: number;
  Total_R_Open_Ai: number;
}
