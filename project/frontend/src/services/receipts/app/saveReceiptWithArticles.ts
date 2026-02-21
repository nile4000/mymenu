import { Article } from "src/helpers/interfaces/article.interface";
import { Receipt } from "src/helpers/interfaces/receipt.interface";
import { convertToISODate } from "src/helpers/dateHelpers";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { insertArticles } from "src/services/articles/infra/articleSupabaseRepo";
import { insertReceipt } from "../infra/receiptSupabaseRepo";

export async function saveReceiptWithArticles(
  articles: Article[],
  receiptData: Receipt
): Promise<ServiceResult<{ receipt: Receipt; articles: unknown[] }>> {
  try {
    const fallbackDate = new Intl.DateTimeFormat("de-DE", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(new Date());

    const rawDate = receiptData?.Purchase_Date || receiptData?.Created_At || fallbackDate;
    const formattedDate = convertToISODate(rawDate);

    const receiptPayload = {
      Uuid: receiptData.Uuid,
      Purchase_Date: formattedDate,
      Corp: receiptData.Corp,
      Total_Receipt: receiptData.Total_Receipt,
    };

    const receiptInsertData = await insertReceipt(receiptPayload);
    const receipt = receiptInsertData[0] as Receipt;

    const articlePayload = articles.map((article) => {
      const price = Number(article.Price ?? 0);
      const quantity = Number(article.Quantity ?? 0);
      const discount = Number(article.Discount ?? 0);
      const total = article.Total !== undefined && article.Total !== null ? Number(article.Total) : price;

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

    const insertedArticles = await insertArticles(articlePayload);

    return ok({
      receipt,
      articles: insertedArticles,
    });
  } catch (cause) {
    return err(
      toServiceError(
        "RECEIPT_SAVE_WITH_ARTICLES_ERROR",
        "Kassenzettel und Artikel konnten nicht gespeichert werden.",
        cause
      )
    );
  }
}
