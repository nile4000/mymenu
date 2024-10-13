import { supabase } from "src/boot/supabase";
import { Receipt } from "../helpers/interfaces/receipt.interface";

export async function readAllReceipts(): Promise<Receipt[] | null> {
  const { data, error } = await supabase.from("receipt").select("*");

  if (error) {
    console.error("Error fetching receipts:", error);
    throw error;
  }

  return data;
}
