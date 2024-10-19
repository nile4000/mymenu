import { supabase } from "src/boot/supabase";

export async function upsertArticleCategories(
  categorizedArticles: { id: string; category: string }[]
) {
  const upsertData = categorizedArticles.map((article) => ({
    Id: article.id,
    Category: article.category,
  }));

  const { data, error } = await supabase
    .from("article")
    .upsert(upsertData, { onConflict: "Id" });

  if (error) {
    console.error("Error upserting article categories:", error);
    throw error;
  }

  return data;
}

export async function upsertArticleDetails(
  unitArticles: {
    id: string;
    unit: string;
    base_unit: number;
    price_base_unit: number;
  }[]
) {
  for (const article of unitArticles) {
    // Check if the article exists
    const { data: existingArticle, error: fetchError } = await supabase
      .from("article")
      .select("Id")
      .eq("Id", article.id)
      .single();

    if (fetchError && fetchError.code !== "PGRST116") {
      console.error("Error fetching article:", fetchError);
      throw fetchError;
    }

    if (existingArticle) {
      const { data, error } = await supabase
        .from("article")
        .update({
          Unit: article.unit,
          Base_Unit: article.base_unit,
          Price_Base_Unit: article.price_base_unit,
        })
        .eq("Id", article.id);

      if (error) {
        console.error("Error updating article details:", error);
        throw error;
      }
    } else {
      // Perform insert with Receipt_Id
      const { data, error } = await supabase.from("article").insert({
        Id: article.id,
        Unit: article.unit,
        Base_Unit: article.base_unit,
        Price_Base_Unit: article.price_base_unit,
        Receipt_Id: "your_receipt_id", // Provide appropriate Receipt_Id
      });

      if (error) {
        console.error("Error inserting article details:", error);
        throw error;
      }
    }
  }
}
