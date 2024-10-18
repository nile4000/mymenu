export interface Article {
  Id: string;
  Receipt_Id?: string; // ist die Receipt.Id
  Name: string;
  Price: number;
  Unit: string;
  Quantity: number;
  Discount: number;
  Total: number;
  Purchase_Date?: string;
  Category?: string;
}
