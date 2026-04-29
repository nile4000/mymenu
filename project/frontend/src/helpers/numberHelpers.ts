export function formatChfAmount(value?: number | string | null): string {
  const amount = Number(value ?? 0);
  return new Intl.NumberFormat("de-CH", { style: "currency", currency: "CHF" }).format(
    Number.isFinite(amount) ? amount : 0
  );
}
