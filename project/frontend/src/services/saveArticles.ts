import { supabase } from "src/boot/supabase";
import { Article } from "../helpers/interfaces/article.interface";
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { convertToISODate } from "../helpers/dateHelpers";

export async function saveArticlesAndReceipt(
  articles: Article[],
  receiptData: Receipt
): Promise<any> {
  try {
    const fallbackDate = new Intl.DateTimeFormat("de-DE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date());
    const rawDate =
      receiptData?.Purchase_Date || receiptData?.Created_At || fallbackDate;
    const formattedDate = convertToISODate(rawDate);
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
      const price = Number(article.Price ?? 0);
      const quantity = Number(article.Quantity ?? 0);
      const discount = Number(article.Discount ?? 0);
      const total =
        article.Total !== undefined && article.Total !== null
          ? Number(article.Total)
          : price;

      // Use an explicit payload to avoid sending unsupported columns (e.g. Id)
      // and to keep object keys stable for PostgREST bulk inserts.
      return {
        Name: article.Name ?? "",
        Price: Number.isFinite(price) ? price : 0,
        Quantity: Number.isFinite(quantity) ? quantity : 0,
        Discount: Number.isFinite(discount) ? discount : 0,
        Total: Number.isFinite(total) ? total : 0,
        Category: article.Category ?? "",
        Unit: article.Unit ?? null,
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
