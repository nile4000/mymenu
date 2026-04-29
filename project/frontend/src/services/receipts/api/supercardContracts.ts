export interface SupercardStatusResponse {
  connected: boolean;
  sessionUpdatedAt?: string | null;
}

export interface SupercardAvailableReceipt {
  receiptUrl: string;
  supercardReceiptBarcode: string;
  locationName?: string | null;
  logoUrl?: string | null;
  purchaseDate?: string | null;
  totalChf?: string | null;
}

export interface SupercardAvailableResponse {
  count: number;
  receipts: SupercardAvailableReceipt[];
}

export interface SupercardSyncResponse {
  importedReceipts: number;
  skippedReceipts: number;
  failedReceipts: number;
  errors: string[];
}

export interface SetSupercardSessionRequest {
  cookieHeader: string;
}
