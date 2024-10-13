import { supabase } from "src/boot/supabase";

export const deleteReceiptById = async (id: string) => {
  const { data, error } = await supabase.from("receipt").delete().eq("Id", id);
  if (error) {
    console.error("Error deleting receipt:", error);
    throw error;
  }

  return data;
};
