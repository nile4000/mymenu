import { supabase } from "src/boot/supabase";

export const deleteArticleById = async (id: string) => {
  const { data, error } = await supabase.from("article").delete().eq("Id", id);
  if (error) {
    console.error("Error deleting article:", error);
    throw error;
  }

  return data;
};
