import { QVueGlobals } from "quasar";

export const handleError = (typ: string, error: unknown, $q: QVueGlobals) => {
  console.error("Error in " + typ + ":", error);
  const detail =
    typeof error === "string" ? error : error instanceof Error ? error.message : undefined;
  $q.notify({
    type: "negative",
    message: detail ? `Fehler bei ${typ}: ${detail}` : `Ein unerwarteter Fehler ist beim ${typ} aufgetreten.`,
  });
};
