import { Receipt } from "src/helpers/interfaces/receipt.interface";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { fetchReceiptsByIds } from "../infra/receiptSupabaseRepo";

export async function listReceiptsByIds(ids: string[]): Promise<ServiceResult<Receipt[]>> {
  if (!ids.length) {
    return ok([]);
  }
  try {
    return ok(await fetchReceiptsByIds(ids));
  } catch (cause) {
    return err(toServiceError("RECEIPT_LIST_BY_IDS_ERROR", "Kassenzettel konnten nicht geladen werden.", cause));
  }
}
