import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import { removeReceiptById } from "../infra/receiptSupabaseRepo";

export async function deleteReceipt(id: string): Promise<ServiceResult<unknown>> {
  if (!id) {
    return err(toServiceError("VALIDATION_ERROR", "Kassenzettel-ID ist erforderlich."));
  }
  try {
    return ok(await removeReceiptById(id));
  } catch (cause) {
    return err(toServiceError("RECEIPT_DELETE_ERROR", "Kassenzettel konnte nicht geloescht werden.", cause));
  }
}
