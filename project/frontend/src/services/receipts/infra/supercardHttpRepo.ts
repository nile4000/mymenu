import { http } from "src/services/shared/infra/http";
import {
  SupercardAvailableResponse,
  SetSupercardSessionRequest,
  SupercardStatusResponse,
  SupercardSyncResponse,
} from "../api/supercardContracts";

export async function postSupercardSession(payload: SetSupercardSessionRequest): Promise<SupercardStatusResponse> {
  const { data } = await http.post<SupercardStatusResponse>("/api/integrations/supercard/session", payload);
  return data;
}

export async function fetchSupercardStatus(): Promise<SupercardStatusResponse> {
  const { data } = await http.get<SupercardStatusResponse>("/api/integrations/supercard/status");
  return data;
}

export async function fetchSupercardAvailable(): Promise<SupercardAvailableResponse> {
  const { data } = await http.get<SupercardAvailableResponse>("/api/integrations/supercard/available");
  return data;
}

export async function postSupercardSync(): Promise<SupercardSyncResponse> {
  const { data } = await http.post<SupercardSyncResponse>("/api/integrations/supercard/sync");
  return data;
}
