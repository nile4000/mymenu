import { supabase } from "src/boot/supabase";
import { Article } from "../helpers/interfaces/article.interface";
import { Receipt } from "../helpers/interfaces/receipt.interface";

// (DD.MM.YY -> YYYY-MM-DD)
function convertToISODate(dateString: string): string {
  const [day, month, year] = dateString.split(".");
  const fullYear = year.length === 2 ? `20${year}` : year;
  return `${fullYear}-${month}-${day}`;
}

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

    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
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

    // save article
    const { data: articlesInsertData, error: articlesInsertError } =
      await supabase.from("article").insert(preparedArticles);

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