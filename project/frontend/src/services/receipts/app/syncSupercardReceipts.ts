import {
  SetSupercardSessionRequest,
  SupercardStatusResponse,
  SupercardSyncResponse,
} from "src/services/receipts/api/supercardContracts";
import { ServiceResult, err, ok } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import {
  fetchSupercardStatus,
  postSupercardSession,
  postSupercardSync,
} from "../infra/supercardHttpRepo";

export async function setSupercardSession(
  payload: SetSupercardSessionRequest
): Promise<ServiceResult<SupercardStatusResponse>> {
  try {
    return ok(await postSupercardSession(payload));
  } catch (cause) {
    return err(
      toServiceError(
        "SUPERCARD_SESSION_SET_ERROR",
        "Supercard Session konnte nicht gespeichert werden.",
        cause
      )
    );
  }
}

export async function getSupercardStatus(): Promise<ServiceResult<SupercardStatusResponse>> {
  try {
    return ok(await fetchSupercardStatus());
  } catch (cause) {
    return err(
      toServiceError(
        "SUPERCARD_STATUS_ERROR",
        "Supercard Status konnte nicht geladen werden.",
        cause
      )
    );
  }
}

export async function syncSupercardReceipts(): Promise<ServiceResult<SupercardSyncResponse>> {
  try {
    return ok(await postSupercardSync());
  } catch (cause) {
    return err(
      toServiceError(
        "SUPERCARD_SYNC_ERROR",
        "Supercard Sync konnte nicht gestartet werden.",
        cause
      )
    );
  }
}
