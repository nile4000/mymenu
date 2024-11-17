export const handleError = (typ: string, error: any, $q: any) => {
  console.error("Error in " + typ + ":", error);
  // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
  $q.notify({
    type: "negative",
    message: `Ein unerwarteter Fehler ist bei der ${typ} aufgetreten.`,
  });
};
