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
    const [day, month, year] = dateString.split(".");
    const fullYear = year.length === 2 ? `20${year}` : year;
    return `${fullYear}-${month}-${day}`;
  } catch (error) {
    return "2024-11-23";
  }
}
