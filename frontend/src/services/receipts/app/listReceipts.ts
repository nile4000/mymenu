import { Receipt } from "src/helpers/interfaces/receipt.interface";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { fetchAllReceipts } from "../infra/receiptSupabaseRepo";

export async function listReceipts(): Promise<ServiceResult<Receipt[]>> {
  try {
    return ok((await fetchAllReceipts()) ?? []);
  } catch (cause) {
    return err(toServiceError("RECEIPT_LIST_ERROR", "Kassenzettel konnten nicht geladen werden.", cause));
  }
}
