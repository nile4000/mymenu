export interface SupercardStatusResponse {
  connected: boolean;
  supercardName?: string | null;
  sessionUpdatedAt?: string | null;
}

export interface SupercardSyncResponse {
  importedReceipts: number;
  skippedReceipts: number;
  failedReceipts: number;
  errors: string[];
}

export interface SetSupercardSessionRequest {
  cookieHeader: string;
  supercardName?: string;
}
