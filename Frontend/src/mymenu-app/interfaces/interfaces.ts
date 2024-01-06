export interface Receipt {
  created_at: string;
  generated_at: string | null;
  id: number;
  user_id: number | null;
}

export interface Item {
  id: number;
  created_at: string;
  receipt_id: number;
  name: string;
  price: number;
  unit: string;
  quantity: string;
}
