import { ServiceError } from "./result";

export function toServiceError(
  code: string,
  message: string,
  cause?: unknown,
  context?: Record<string, unknown>
): ServiceError {
  return { code, message, cause, context };
}
