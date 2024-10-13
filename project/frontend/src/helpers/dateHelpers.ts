export function formatDate(dateString: string) {
  if (!dateString) return "";
  const date = new Date(dateString.toString().trim());
  return new Intl.DateTimeFormat("de-DE", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(date);
}
export function formatMonth(monthNumber: number): string {
  const monthNames = [
    "Januar",
    "Februar",
    "März",
    "April",
    "Mai",
    "Juni",
    "Juli",
    "August",
    "September",
    "Oktober",
    "November",
    "Dezember",
  ];
  return monthNames[monthNumber - 1] || "Unbekannt";
}
