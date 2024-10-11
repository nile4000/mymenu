import { supabase } from "src/boot/supabase";
import { Article } from "../helpers/interfaces/article.interface";

export async function readAllArticles(): Promise<Article[] | null> {
  const { data, error } = await supabase.from("article").select("*");

  if (error) {
    console.error("Error fetching articles:", error);
    throw error;
  }

  return data;
}
