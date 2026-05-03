import { computed, onMounted, reactive } from "vue";
import { useQuasar } from "quasar";
import { handleError } from "src/helpers/composables/useErrors";
import { hideLoading, showLoading } from "src/helpers/composables/useLoader";
import { getSupercardAvailable, getSupercardStatus, setSupercardSession, syncSupercardReceipts } from "src/services";
import type { SupercardAvailableReceipt, SupercardSyncResponse } from "src/services/receipts/api/supercardContracts";

type SyncSummaryTone = "positive" | "warning";

type UseSupercardSyncOptions = {
  onSynced?: () => void;
};

function buildSyncSummary(result: SupercardSyncResponse): { message: string; tone: SyncSummaryTone } {
  if (result.importedReceipts === 0 && result.failedReceipts === 0) {
    return {
      message: result.deferredReceipts > 0
        ? `Keine neuen Belege importiert. Noch ${result.deferredReceipts} ausstehend.`
        : "Alle Belege bereits importiert.",
      tone: "positive",
    };
  }

  const parts = [`${result.importedReceipts} importiert`];
  if (result.deferredReceipts > 0) {
    parts.push(`${result.deferredReceipts} ausstehend`);
  }
  if (result.failedReceipts > 0) {
    parts.push(`${result.failedReceipts} fehlgeschlagen`);
  }

  return {
    message: `${result.failedReceipts > 0 ? "Teilweise abgeschlossen" : "Erfolg"}: ${parts.join(", ")}.`,
    tone: result.failedReceipts > 0 ? "warning" : "positive",
  };
}

export function useSupercardSync(options: UseSupercardSyncOptions = {}) {
  const $q = useQuasar();

  const cookieForm = reactive({
    visible: false,
    value: "",
  });

  const session = reactive({
    connected: null as boolean | null,
    updatedAt: "" as string | null | undefined,
  });

  const preview = reactive({
    loaded: false,
    receipts: [] as SupercardAvailableReceipt[],
  });

  const requests = reactive({
    saveSession: false,
    loadPreview: false,
    syncReceipts: false,
  });

  const sync = reactive({
    summary: "",
    summaryTone: "positive" as SyncSummaryTone,
    errors: [] as string[],
  });

  const availableReceiptsCount = computed(() => preview.receipts.length);

  function clearAvailableReceipts() {
    preview.receipts = [];
    preview.loaded = false;
  }

  async function loadAvailableReceipts() {
    requests.loadPreview = true;
    try {
      const result = await getSupercardAvailable();

      if (result.ok) {
        preview.receipts = result.data.receipts;
        preview.loaded = true;
      } else {
        handleError("Belege laden", result.error.message, $q);
      }
    } finally {
      requests.loadPreview = false;
    }
  }

  async function connectSupercard() {
    if (cookieForm.value.length < 20) return;

    requests.saveSession = true;
    try {
      const result = await setSupercardSession({ cookieHeader: cookieForm.value.trim() });

      if (result.ok) {
        session.connected = true;
        session.updatedAt = result.data.sessionUpdatedAt;
        cookieForm.visible = false;
        sync.summary = "";
        sync.errors = [];
        clearAvailableReceipts();
      } else {
        handleError("Verbindung", result.error.message, $q);
      }
    } finally {
      requests.saveSession = false;
    }
  }

  async function runSupercardSync() {
    requests.syncReceipts = showLoading("Supercard Belege werden importiert...", $q);
    try {
      const result = await syncSupercardReceipts();

      if (result.ok) {
        const summary = buildSyncSummary(result.data);
        sync.summary = summary.message;
        sync.summaryTone = summary.tone;
        sync.errors = result.data.errors;

        $q.notify({ type: summary.tone, message: summary.message });
        clearAvailableReceipts();
        options.onSynced?.();
      } else {
        handleError("Supercard Sync", result.error.message, $q);
      }
    } finally {
      requests.syncReceipts = hideLoading($q);
    }
  }

  function resetSupercardDraft() {
    cookieForm.value = "";
    clearAvailableReceipts();
    cookieForm.visible = true;
  }

  onMounted(async () => {
    const status = await getSupercardStatus();
    if (status.ok && status.data.connected) {
      session.connected = true;
      session.updatedAt = status.data.sessionUpdatedAt;
    } else {
      session.connected = false;
    }
  });

  return {
    availableReceiptsCount,
    cookieForm,
    session,
    preview,
    requests,
    sync,
    connectSupercard,
    loadAvailableReceipts,
    resetSupercardDraft,
    runSupercardSync,
  };
}
