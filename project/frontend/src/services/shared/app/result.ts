export type ServiceError = {
  code: string;
  message: string;
  cause?: unknown;
  context?: Record<string, unknown>;
};

export type ServiceResult<T> = { ok: true; data: T } | { ok: false; error: ServiceError };

export function ok<T>(data: T): ServiceResult<T> {
  return { ok: true, data };
}

export function err<T = never>(error: ServiceError): ServiceResult<T> {
  return { ok: false, error };
}
