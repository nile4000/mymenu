import { ExtractUnitResultItem } from "../api/aiContracts";
import { postExtractUnitTask } from "../infra/aiHttpClient";
import { createBatches } from "src/services/shared/app/batch";
import { withRetry } from "src/services/shared/app/retry";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";

export function prepareArticlesPrices(
  items: { Id: string; Name: string; Quantity: number; Total?: number; Price?: number }[]
) {
  return items.map((item) => ({
    id: String(item.Id),
    name: item.Name,
    quantity: item.Quantity,
    price: item.Total ?? item.Price ?? 0,
  }));
}

export async function extractArticleUnits(
  items: { Id: string; Name: string; Quantity: number; Total?: number; Price?: number }[],
  handleBatchResponse: (data: ExtractUnitResultItem[]) => Promise<void>
): Promise<ServiceResult<void>> {
  try {
    const preparedItems = prepareArticlesPrices(items);
    const batches = createBatches(preparedItems, 40);

    for (const batch of batches) {
      const response = await withRetry(() => postExtractUnitTask("extract-unit", batch));
      await handleBatchResponse(response.data);
    }

    return ok(undefined);
  } catch (cause) {
    const message =
      typeof (cause as { response?: { data?: { message?: unknown } } })?.response?.data?.message === "string"
        ? String((cause as { response?: { data?: { message?: unknown } } }).response?.data?.message)
        : "Einheitsextraktion fehlgeschlagen.";
    return err(toServiceError("AI_EXTRACT_UNIT_ERROR", message, cause));
  }
}
