import { supabase } from "src/boot/supabase";
import { Article } from "../helpers/interfaces/article.interface";

export async function saveArticles(articles: Article[]): Promise<any> {
  const preparedArticles = articles.map((article) => {
    const total =
      article.Total !== undefined && article.Total !== null
        ? article.Total
        : article.Price * article.Quantity;

    const formattedDate = new Date(article.Purchase_Date || new Date())
      .toISOString()
      .split("T")[0];

    return {
      ...article,
      Total: total,
      Purchase_Date: formattedDate,
    };
  });

  const { data, error } = await supabase
    .from("article")
    .insert(preparedArticles);

  if (error) {
    console.error("Error inserting articles:", error);
    throw error;
  }

  return data;
}
