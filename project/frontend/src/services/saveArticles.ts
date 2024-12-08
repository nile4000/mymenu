import { supabase } from "src/boot/supabase";
import { Article } from "../helpers/interfaces/article.interface";
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { convertToISODate } from "../helpers/dateHelpers";

export async function saveArticlesAndReceipt(
  articles: Article[],
  receiptData: Receipt
): Promise<any> {
  try {
    const formattedDate = convertToISODate(receiptData.Purchase_Date);
    // save receipt
    const { data: receiptInsertData, error: receiptInsertError } =
      await supabase
        .from("receipt")
        .insert([
          {
            Uuid: receiptData.Uuid,
            Purchase_Date: formattedDate,
            Corp: receiptData.Corp,
            Total_Receipt: receiptData.Total_Receipt,
          },
        ])
        .select();

    if (receiptInsertError) {
      console.error("Error inserting receipt:", receiptInsertError);
      throw receiptInsertError;
    }

    const receipt: Receipt = receiptInsertData[0];

    const preparedArticles = articles.map((article) => {
      const total =
        article.Total !== undefined && article.Total !== null
          ? article.Total
          : article.Price;

      return {
        ...article,
        Total: total,
        Purchase_Date: formattedDate,
        Receipt_Id: receipt.Id,
      };
    });

    // save and return safed article with Id,Name,Quantity and Price
    const { data: articlesInsertData, error: articlesInsertError } =
      await supabase
        .from("article")
        .insert(preparedArticles)
        .select("Id,Name,Quantity,Price");

    if (articlesInsertError) {
      console.error("Error inserting articles:", articlesInsertError);
      throw articlesInsertError;
    }

    return { receipt, articles: articlesInsertData };
  } catch (error) {
    console.error("Error saving receipt and articles:", error);
    throw error;
  }
}
