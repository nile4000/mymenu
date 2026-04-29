<template>
  <q-page class="column items-center q-pa-md bg-grey-1">
    <div class="text-h5 q-mb-md">Scanner</div>
    <ScannerPage class="q-mb-xl" />

    <div class="supercard-card shadow-2 q-pa-md">
      <div class="row items-center justify-between q-mb-md">
        <div class="column">
          <span class="text-subtitle1 text-bold text-primary">Supercard Sync</span>
          <span class="text-caption text-grey-7">Automatischer Beleg-Import</span>
        </div>
        <q-chip
          dense
          square
          :color="supercardConnected ? 'positive' : 'orange'"
          text-color="white"
          class="text-weight-bold"
        >
          {{ supercardConnected ? "Verbunden" : "Session fehlt" }}
        </q-chip>
      </div>

      <div class="step-container q-mb-sm">
        <div class="row items-center no-wrap q-gutter-sm">
          <div class="step-badge">1</div>
          <div class="col">
            <div class="text-weight-bold">Zugangsdaten</div>
            <div class="text-caption text-grey-8 truncate-ts">
              {{
                supercardConnected
                  ? `Aktiv seit: ${formatStatusTimestamp(supercardStatusText)}`
                  : "Cookie aus DevTools einfügen"
              }}
            </div>
          </div>
          <q-btn
            flat
            dense
            color="primary"
            :icon="showCookieInput ? 'expand_less' : 'edit'"
            @click="showCookieInput = !showCookieInput"
          />
        </div>

        <q-slide-transition>
          <div v-if="showCookieInput || !supercardConnected" class="q-mt-sm">
            <q-input
              v-model="supercardCookie"
              outlined
              dense
              type="textarea"
              autogrow
              label="Cookie Header einfügen"
              placeholder="JSESSIONID=...; "
              class="bg-white"
            />
            <div class="row q-gutter-sm q-mt-xs">
              <q-btn
                unelevated
                color="primary"
                label="Verbindung speichern"
                class="col"
                :loading="isConnecting"
                @click="connectSupercard"
              />
              <q-btn flat dense color="grey-7" icon="delete_sweep" @click="resetSupercardDraft" />
            </div>
          </div>
        </q-slide-transition>
      </div>

      <div v-if="supercardConnected" class="step-container q-mb-sm">
        <div class="row items-center no-wrap q-gutter-sm">
          <div class="step-badge">2</div>
          <div class="col">
            <div class="text-weight-bold">Vorschau</div>
            <div class="text-caption text-grey-8">{{ availableReceiptsCount }} neue Belege gefunden</div>
          </div>
          <q-btn
            flat
            round
            dense
            icon="refresh"
            color="secondary"
            :loading="isLoadingAvailable"
            @click="loadAvailableReceipts"
          />
        </div>

        <q-scroll-area
          v-if="availableReceiptsLoaded && availableReceipts.length"
          style="height: 180px"
          class="q-mt-sm border-grey rounded-borders bg-white"
        >
          <q-list dense separator>
            <q-item v-for="receipt in availableReceipts" :key="receipt.externalReceiptId">
              <q-item-section avatar>
                <q-img :src="receipt.logoUrl || ''" width="28px" height="28px" fit="contain" />
              </q-item-section>
              <q-item-section>
                <q-item-label class="text-caption text-weight-medium">{{ receipt.locationName }}</q-item-label>
                <q-item-label caption>{{ formatDateShort(receipt.purchaseDate) }}</q-item-label>
              </q-item-section>
              <q-item-section side class="text-black text-weight-bold">
                {{ formatAvailableAmount(receipt.totalChf) }}
              </q-item-section>
            </q-item>
          </q-list>
        </q-scroll-area>
      </div>

      <div v-if="availableReceiptsCount > 0" class="q-mt-md">
        <q-btn
          unelevated
          color="accent"
          size="lg"
          class="full-width text-bold shadow-2"
          :loading="isSyncing"
          @click="runSupercardSync"
        >
          <q-icon name="cloud_download" class="q-mr-sm" />
          {{ availableReceiptsCount }} Belege importieren
        </q-btn>
      </div>

      <div v-if="lastSyncSummary" class="text-center text-caption q-mt-sm text-grey-7">
        {{ lastSyncSummary }}
      </div>
    </div>

    <div class="full-width q-px-md" style="max-width: 1200px">
      <h5 class="text-center q-mt-xl q-mb-md">Meine Kassenzettel</h5>
      <q-table
        grid
        flat
        :rows="rows"
        :columns="columns"
        row-key="Id"
        :pagination="{ rowsPerPage: 12 }"
        class="justify-center"
      >
        <template v-slot:item="props">
          <q-card class="receipt-card q-ma-sm shadow-1">
            <q-card-section class="text-center q-py-sm">
              <q-img
                :src="props.row.Corp === 'Coop' ? '/assets/coop.png' : '/assets/migros.png'"
                style="width: 45px"
                fit="contain"
              />
            </q-card-section>

            <q-separator inset />

            <q-card-section class="q-pa-md">
              <div class="row justify-between text-caption text-grey-7">
                <span>Gekauft:</span>
                <span class="text-black text-weight-medium">{{ formatDateShort(props.row.Purchase_Date) }}</span>
              </div>
              <div class="row justify-between q-mt-xs">
                <span class="text-subtitle1 text-bold">{{ props.row.Total_Receipt.toFixed(2) }} CHF</span>
              </div>

              <q-img
                v-if="props.row.Thumbnail"
                :src="props.row.Thumbnail"
                class="q-mt-sm rounded-borders border-grey"
                style="height: 100px"
                fit="cover"
              />
            </q-card-section>

            <q-card-actions align="around" class="q-pt-none">
              <q-btn flat round dense icon="visibility" color="grey-7" disabled />
              <q-btn flat round dense icon="delete" color="negative" @click="deleteReceipt(props.row)" />
            </q-card-actions>
          </q-card>
        </template>
      </q-table>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { storeToRefs } from "pinia";
import { useQuasar } from "quasar";
import { useDataStore } from "../../stores/dataStore";
import ScannerPage from "../scanner/ScannerPage.vue";
import { formatDateShort, formatDate } from "../../helpers/dateHelpers";
import {
  deleteReceipt as deleteReceiptService,
  getSupercardAvailable,
  getSupercardStatus,
  setSupercardSession,
  syncSupercardReceipts,
} from "../../services";
import { handleError } from "src/helpers/composables/useErrors";
import type { Receipt } from "../../helpers/interfaces/receipt.interface";

// Store & Quasar
const $q = useQuasar();
const dataStore = useDataStore();
const { receipts: rows } = storeToRefs(dataStore);

// States
const showCookieInput = ref(false);
const supercardCookie = ref("");
const supercardConnected = ref(false);
const supercardStatusText = ref("");
const lastSyncSummary = ref("");
const isConnecting = ref(false);
const isLoadingAvailable = ref(false);
const isSyncing = ref(false);
const availableReceipts = ref<any[]>([]);
const availableReceiptsLoaded = ref(false);

const availableReceiptsCount = computed(() => availableReceipts.value.length);

// Formatierungshilfen
const formatStatusTimestamp = (val: string) => (val ? new Date(val).toLocaleString("de-CH") : "Nie");
const formatAvailableAmount = (val: any) =>
  new Intl.NumberFormat("de-CH", { style: "currency", currency: "CHF" }).format(Number(val));

// Actions
const loadAvailableReceipts = async () => {
  isLoadingAvailable.value = true;
  const result = await getSupercardAvailable();
  isLoadingAvailable.value = false;
  if (result.ok) {
    availableReceipts.value = result.data.receipts;
    availableReceiptsLoaded.value = true;
  } else {
    handleError("Belege laden", result.error.message, $q);
  }
};

const connectSupercard = async () => {
  if (supercardCookie.value.length < 20) return;
  isConnecting.value = true;
  const result = await setSupercardSession({ cookieHeader: supercardCookie.value.trim() });
  isConnecting.value = false;

  if (result.ok) {
    supercardConnected.value = true;
    supercardStatusText.value = result.data.sessionUpdatedAt;
    showCookieInput.value = false;
    await loadAvailableReceipts();
  } else {
    handleError("Verbindung", result.error.message, $q);
  }
};

const runSupercardSync = async () => {
  isSyncing.value = true;
  const result = await syncSupercardReceipts();
  isSyncing.value = false;
  if (result.ok) {
    lastSyncSummary.value = `Erfolg: ${result.data.importedReceipts} importiert.`;
    dataStore.initialized = false;
    await dataStore.ensureInitialized();
    await loadAvailableReceipts();
  }
};

const resetSupercardDraft = () => {
  supercardCookie.value = "";
  availableReceipts.value = [];
  showCookieInput.value = true;
};

const deleteReceipt = async (receipt: Receipt) => {
  if (confirm("Löschen?")) {
    const result = await deleteReceiptService(receipt.Id!);
    if (result.ok) dataStore.removeReceiptById(receipt.Id!);
  }
};

const columns = [{ name: "Purchase_Date", field: "Purchase_Date", sortable: true }];

onMounted(async () => {
  await dataStore.ensureInitialized();
  const status = await getSupercardStatus();
  if (status.ok && status.data.connected) {
    supercardConnected.value = true;
    supercardStatusText.value = status.data.sessionUpdatedAt;
    loadAvailableReceipts();
  }
  dataStore.startRealtime();
});
</script>

<style scoped lang="scss">
.supercard-card {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.step-container {
  background: #fcfcfc;
  border: 1px solid #efefef;
  border-radius: 12px;
  padding: 12px;
}

.step-badge {
  width: 26px;
  height: 26px;
  background: $primary;
  color: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 13px;
}

.border-grey {
  border: 1px solid #ececec;
}

.receipt-card {
  width: 200px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
  }
}

.truncate-ts {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
