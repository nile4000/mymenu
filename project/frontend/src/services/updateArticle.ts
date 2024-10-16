import { supabase } from "src/boot/supabase";

export async function updateArticleCategoryById(
  categorizedArticles: { id: string; category: string }[]
) {
  const updates = [];

  for (const article of categorizedArticles) {
    const { data, error } = await supabase
      .from("article")
      .update({
        Category: article.category,
      })
      .eq("Id", article.id);

    if (error) {
      console.error(`Error updating article with ID ${article.id}:`, error);
      throw error;
    }

    updates.push(data);
  }

  return updates;
}

export async function updateArticleDetailsById(
  unitArticles: { id: string; unit: string }[]
) {
  const updates = [];

  for (const article of unitArticles) {
    const { data, error } = await supabase
      .from("article")
      .update({
        Unit: article.unit,
      })
      .eq("Id", article.id);

    if (error) {
      console.error(`Error updating article with ID ${article.id}:`, error);
      throw error;
    }

    updates.push(data);
  }

  return updates;
}
