import { supabase } from "src/boot/supabase";

/**
 * Batch updates categories for multiple articles.
 * @param categorizedArticles Array of articles with their new categories.
 * @returns The updated data from Supabase.
 */
export async function upsertArticleCategories(
  categorizedArticles: { id: string; category: string }[]
) {
  if (!categorizedArticles.length) return;

  // Prepare the updates
  const updates = categorizedArticles.map((article) => ({
    Id: article.id,
    Category: article.category,
  }));

  if (!updates.length) return;

  // Perform upsert
  const { data, error } = await supabase
    .from("article")
    .upsert(updates);

  if (error) {
    console.error(`Error updating article categories:`, error);
    throw error;
  }

  return data;
}

/**
 * Batch updates units for multiple articles.
 * @param unitArticles Array of articles with their new units.
 * @returns The updated data from Supabase.
 */
export async function upsertArticleUnit(
  unitArticles: { id: string; unit: string }[]
) {
  if (!unitArticles.length) return;

  const updates = unitArticles.map((article) => ({
    Id: article.id,
    Unit: article.unit,
  }));

  if (!updates) return;

  const { data, error } = await supabase
    .from("article")
    .upsert(updates,);

  if (error) {
    console.error(`Error updating article units:`, error);
    throw error;
  }

  return data;
}
