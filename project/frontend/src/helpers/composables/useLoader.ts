import { QVueGlobals } from "quasar";

export const showLoading = (message: string, $q: QVueGlobals): boolean => {
  $q.loading.show({
    message: message,
    backgroundColor: "rgba(0, 0, 0, 0.5)",
    spinnerColor: "white",
  });

  return true;
};

export const hideLoading = ($q: QVueGlobals): boolean => {
  $q.loading.hide();
  return false;
};
