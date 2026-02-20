export function formatDate(dateString: string) {
  if (!dateString) return dateString || "";

  const date = new Date(dateString.trim());

  if (isNaN(date.getTime())) {
    return dateString || "";
  }

  // Format the valid date
  return new Intl.DateTimeFormat("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(date);
}

export function formatDateShort(dateString: string) {
  if (!dateString) return dateString || "";

  const date = new Date(dateString.trim());

  if (isNaN(date.getTime())) {
    return dateString || "";
  }

  // Format the valid date
  return new Intl.DateTimeFormat("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "2-digit",
  }).format(date);
}

export function formatMonth(monthNumber: number): string {
  const monthNames = [
    "Jan.",
    "Feb.",
    "März",
    "April",
    "Mai",
    "Juni",
    "Juli",
    "Aug.",
    "Sept.",
    "Okt.",
    "Nov.",
    "Dez.",
  ];
  return monthNames[monthNumber - 1] || "Unbekannt";
}

// (DD.MM.YY -> YYYY-MM-DD)
export function convertToISODate(dateString: string): string {
  try {
    const value = (dateString || "").trim();
    if (!value) throw new Error("empty date");

    if (value.includes(".")) {
      const [day, month, year] = value.split(".");
      if (!day || !month || !year) throw new Error("invalid dotted date");
      const fullYear = year.length === 2 ? `20${year}` : year;
      return `${fullYear}-${month.padStart(2, "0")}-${day.padStart(2, "0")}`;
    }

    const parsed = new Date(value);
    if (isNaN(parsed.getTime())) throw new Error("invalid date");

    const yyyy = parsed.getFullYear();
    const mm = String(parsed.getMonth() + 1).padStart(2, "0");
    const dd = String(parsed.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  } catch (error) {
    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, "0");
    const dd = String(now.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  }
}
