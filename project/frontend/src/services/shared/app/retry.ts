// Retry helper with fixed backoff: waits `retryDelay` between attempts so transient errors can recover.
// Note: `maxRetries` limits total attempts (including the first).
export const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

export async function withRetry<T>(action: () => Promise<T>, maxRetries = 2, retryDelay = 2000): Promise<T> {
  let attempt = 0;
  while (true) {
    try {
      return await action();
    } catch (error) {
      attempt += 1;
      if (attempt >= maxRetries) {
        throw error;
      }
      await delay(retryDelay);
    }
  }
}
