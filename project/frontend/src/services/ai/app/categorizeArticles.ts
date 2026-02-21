import { CategorizeResultItem } from "../api/aiContracts";
import { postAiTask } from "../infra/aiHttpClient";
import { createBatches } from "src/services/shared/app/batch";
import { withRetry } from "src/services/shared/app/retry";
import { ServiceResult, ok, err } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";

export function prepareArticles(items: { Id: string; Name: string }[]) {
  return items.map((item) => ({ id: String(item.Id), name: item.Name }));
}

export async function categorizeArticles(
  items: { Id: string; Name: string }[],
  handleBatchResponse: (data: CategorizeResultItem[]) => Promise<void>
): Promise<ServiceResult<void>> {
  try {
    const preparedItems = prepareArticles(items);
    const batches = createBatches(preparedItems, 40);

    for (const batch of batches) {
      const response = await withRetry(() => postAiTask("categorize", batch));
      await handleBatchResponse(response.data);
    }

    return ok(undefined);
  } catch (cause) {
    const message =
      typeof (cause as { response?: { data?: { message?: unknown } } })?.response?.data?.message === "string"
        ? String((cause as { response?: { data?: { message?: unknown } } }).response?.data?.message)
        : "Kategorisierung fehlgeschlagen.";
    return err(toServiceError("AI_CATEGORIZE_ERROR", message, cause));
  }
}
