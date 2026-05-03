import { supabase } from "src/boot/supabase";
import { Receipt } from "src/helpers/interfaces/receipt.interface";

export async function fetchAllReceipts(): Promise<Receipt[] | null> {
  const { data, error } = await supabase.from("receipt").select("*");
  if (error) throw error;
  return data;
}

export async function fetchReceiptsByIds(ids: string[]): Promise<Receipt[]> {
  const { data, error } = await supabase.from("receipt").select("*").in("Id", ids);
  if (error) throw error;
  return data;
}

export async function removeReceiptById(id: string) {
  const { data, error } = await supabase.from("receipt").delete().eq("Id", id);
  if (error) throw error;
  return data;
}

export async function insertReceipt(payload: Record<string, unknown>) {
  const { data, error } = await supabase.from("receipt").insert([payload]).select();
  if (error) throw error;
  return data;
}
