import { supabase } from "src/boot/supabase";
import { Article } from "../helpers/interfaces/article.interface";
import { Receipt } from "../helpers/interfaces/receipt.interface";

export async function readAllArticles(): Promise<Article[] | null> {
  const { data, error } = await supabase.from("article").select("*");

  if (error) {
    console.error("Error fetching articles:", error);
    throw error;
  }

  return data;
}

export const readReceiptsByIds = async (ids: string[]): Promise<Receipt[]> => {
  const { data, error } = await supabase
    .from("receipt")
    .select("*")
    .in("Id", ids);
  if (error) throw error;
  return data;
};
