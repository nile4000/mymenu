import { QVueGlobals } from "quasar";

export const handleError = (typ: string, error: unknown, $q: QVueGlobals) => {
  console.error("Error in " + typ + ":", error);
  $q.notify({
    type: "negative",
    message: `Ein unerwarteter Fehler ist bei der ${typ} aufgetreten.`,
  });
};
