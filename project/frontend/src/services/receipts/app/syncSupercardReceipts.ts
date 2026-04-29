import {
  SupercardAvailableResponse,
  SetSupercardSessionRequest,
  SupercardStatusResponse,
  SupercardSyncResponse,
} from "src/services/receipts/api/supercardContracts";
import { ServiceResult, err, ok } from "src/services/shared/app/result";
import { toServiceError } from "src/services/shared/app/serviceError";
import {
  fetchSupercardAvailable,
  fetchSupercardStatus,
  postSupercardSession,
  postSupercardSync,
} from "../infra/supercardHttpRepo";

type ErrorPayload = {
  details?: unknown;
  message?: unknown;
  error?: unknown;
};

function firstNonBlankString(...values: unknown[]): string | undefined {
  for (const value of values) {
    if (typeof value === "string" && value.trim()) {
      return value.trim();
    }
  }
  return undefined;
}

function extractErrorMessage(cause: unknown): string | undefined {
  const responseData = (cause as { response?: { data?: unknown } } | undefined)?.response?.data;
  if (typeof responseData === "string" && responseData.trim()) {
    return responseData.trim();
  }

  if (responseData && typeof responseData === "object") {
    const payload = responseData as ErrorPayload;
    return firstNonBlankString(payload.details, payload.message, payload.error);
  }

  if (cause instanceof Error) {
    return firstNonBlankString(cause.message);
  }

  return firstNonBlankString((cause as { message?: unknown } | undefined)?.message);
}

function mapSupercardMessage(fallback: string, cause: unknown): string {
  const message = extractErrorMessage(cause);
  if (!message) {
    return fallback;
  }

  const normalized = message.toLowerCase();
  if (normalized.includes("invalid or expired")) {
    return "Supercard Session ist ungueltig oder abgelaufen. Bitte Cookie erneuern.";
  }
  if (normalized.includes("no supercard session configured")) {
    return "Keine Supercard Session hinterlegt. Bitte zuerst verbinden.";
  }
  if (normalized.includes("missing supercard db config")) {
    return "Supercard Backend ist nicht vollstaendig konfiguriert.";
  }
  if (normalized.includes("cookieheader must not be blank")) {
    return "Bitte einen vollstaendigen Session Cookie Header einfuegen.";
  }

  return message;
}

export async function setSupercardSession(
  payload: SetSupercardSessionRequest
): Promise<ServiceResult<SupercardStatusResponse>> {
  try {
    return ok(await postSupercardSession(payload));
  } catch (cause) {
    return err(
      toServiceError(
        "SUPERCARD_SESSION_SET_ERROR",
        mapSupercardMessage("Supercard Session konnte nicht gespeichert werden.", cause),
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
        mapSupercardMessage("Supercard Status konnte nicht geladen werden.", cause),
        cause
      )
    );
  }
}

export async function getSupercardAvailable(): Promise<ServiceResult<SupercardAvailableResponse>> {
  try {
    return ok(await fetchSupercardAvailable());
  } catch (cause) {
    return err(
      toServiceError(
        "SUPERCARD_AVAILABLE_ERROR",
        mapSupercardMessage("Verfuegbare Supercard Belege konnten nicht geladen werden.", cause),
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
        mapSupercardMessage("Supercard Sync konnte nicht gestartet werden.", cause),
        cause
      )
    );
  }
}
