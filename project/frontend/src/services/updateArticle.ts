import { supabase } from "src/boot/supabase";
import { Article } from "src/helpers/interfaces/article.interface";

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
  const { data, error } = await supabase.from("article").upsert(updates);

  if (error) {
    console.error(`Error updating article categories:`, error);
    throw error;
  }

  return data;
}

export async function upsertArticleCategory(categorizedArticle: Article) {
  // Prepare the updates
  const update = {
    Id: categorizedArticle.Id,
    Category: categorizedArticle.Category,
  };

  if (!categorizedArticle.Category) return;

  // Perform upsert
  const { data, error } = await supabase.from("article").upsert(update);

  if (error) {
    console.error(`Error updating article categories:`, error);
    throw error;
  }

  return data;
}

export async function upsertArticleUnit(id: string, unit?: string) {
  if (!id || !unit) {
    console.error("Artikel-ID und Einheit müssen vorhanden sein.");
    return;
  }
  const update = {
    Id: id,
    Unit: unit,
  };
  const { data, error } = await supabase.from("article").upsert(update);
  if (error) {
    console.error(`Error updating article unit:`, error);
    throw error;
  }

  return data;
}

/**
 * Batch updates units for multiple articles.
 * @param unitArticles Array of articles with their new units.
 * @returns The updated data from Supabase.
 */
export async function upsertArticleUnits(
  unitArticles: { id: string; unit: string }[]
) {
  if (!unitArticles.length) return;

  const updates = unitArticles.map((article) => ({
    Id: article.id,
    Unit: article.unit,
  }));

  if (!updates) return;

  const { data, error } = await supabase.from("article").upsert(updates);

  if (error) {
    console.error(`Error updating article units:`, error);
    throw error;
  }

  return data;
}
