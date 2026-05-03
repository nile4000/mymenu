const DATE_LOCALE = "de-CH";

const longDateFormatter = new Intl.DateTimeFormat(DATE_LOCALE, {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
});

const shortDateFormatter = new Intl.DateTimeFormat(DATE_LOCALE, {
  day: "2-digit",
  month: "2-digit",
  year: "2-digit",
});

const dateTimeFormatter = new Intl.DateTimeFormat(DATE_LOCALE, {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
});

const monthFormatter = new Intl.DateTimeFormat(DATE_LOCALE, {
  month: "short",
});

function parseDate(value?: string | null): Date | null {
  const trimmed = value?.trim();
  if (!trimmed) return null;

  const date = new Date(trimmed);
  return Number.isNaN(date.getTime()) ? null : date;
}

function formatWithFallback(value: string | null | undefined, formatter: Intl.DateTimeFormat): string {
  const date = parseDate(value);
  return date ? formatter.format(date) : value || "";
}

function toIsoDateParts(date: Date): string {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

export function formatDate(dateString: string) {
  return formatWithFallback(dateString, longDateFormatter);
}

export function formatDateShort(dateString: string) {
  return formatWithFallback(dateString, shortDateFormatter);
}

export function formatDateTimeCh(value?: string | null): string {
  const date = parseDate(value);
  return date ? dateTimeFormatter.format(date) : "Nie";
}

export function formatOptionalDateShort(value?: string | null): string {
  return formatDateShort(value ?? "");
}

export function formatMonth(monthNumber: number): string {
  if (!Number.isInteger(monthNumber) || monthNumber < 1 || monthNumber > 12) {
    return "Unbekannt";
  }
  return monthFormatter.format(new Date(2000, monthNumber - 1, 1));
}

// Converts display dates like DD.MM.YY/DD.MM.YYYY or parseable date strings to YYYY-MM-DD.
export function convertToISODate(dateString: string): string {
  try {
    const value = (dateString || "").trim();
    if (!value) throw new Error("empty date");

    if (value.includes(".")) {
      const [day, month, year] = value.split(".");
      if (!day || !month || !year) throw new Error("invalid dotted date");

      const fullYear = Number(year.length === 2 ? `20${year}` : year);
      const monthNumber = Number(month);
      const dayNumber = Number(day);
      const parsed = new Date(fullYear, monthNumber - 1, dayNumber);

      if (
        parsed.getFullYear() !== fullYear ||
        parsed.getMonth() !== monthNumber - 1 ||
        parsed.getDate() !== dayNumber
      ) {
        throw new Error("invalid dotted date");
      }

      return toIsoDateParts(parsed);
    }

    const parsed = parseDate(value);
    if (!parsed) throw new Error("invalid date");
    return toIsoDateParts(parsed);
  } catch (error) {
    return toIsoDateParts(new Date());
  }
}
