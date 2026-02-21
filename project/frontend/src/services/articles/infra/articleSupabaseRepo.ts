import { supabase } from "src/boot/supabase";
import { Article } from "src/helpers/interfaces/article.interface";

export async function fetchAllArticles(): Promise<Article[] | null> {
  const { data, error } = await supabase.from("article").select("*");
  if (error) throw error;
  return data;
}

export async function upsertArticleCategories(payload: { id: string; category: string }[]) {
  const updates = payload.map((article) => ({ Id: article.id, Category: article.category }));
  const { data, error } = await supabase.from("article").upsert(updates);
  if (error) throw error;
  return data;
}

export async function upsertArticleCategory(payload: { id: string; category: string }) {
  const { data, error } = await supabase.from("article").upsert({ Id: payload.id, Category: payload.category });
  if (error) throw error;
  return data;
}

export async function upsertArticleUnit(payload: { id: string; unit: string }) {
  const { data, error } = await supabase.from("article").upsert({ Id: payload.id, Unit: payload.unit });
  if (error) throw error;
  return data;
}

export async function upsertArticleUnits(payload: { id: string; unit: string }[]) {
  const updates = payload.map((article) => ({ Id: article.id, Unit: article.unit }));
  const { data, error } = await supabase.from("article").upsert(updates);
  if (error) throw error;
  return data;
}

export async function removeArticleById(id: string) {
  const { data, error } = await supabase.from("article").delete().eq("Id", id);
  if (error) throw error;
  return data;
}

export async function insertArticles(payload: Record<string, unknown>[]) {
  const { data, error } = await supabase.from("article").insert(payload).select("Id,Name,Quantity,Price");
  if (error) throw error;
  return data;
}
