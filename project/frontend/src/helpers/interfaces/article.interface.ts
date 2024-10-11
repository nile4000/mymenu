export interface Article {
  Id: string;
  Name: string;
  Price: number;
  Quantity: number;
  Discount: number;
  Total: number;
  Purchase_Date?: string;
  Category?: string;
}
