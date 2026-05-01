<template>
  <q-page class="column items-center q-pa-md bg-grey-1">
    <div class="text-h5 q-mb-md">Scanner</div>

    <div class="row q-gutter-lg q-mb-xl items-start justify-center full-width" style="max-width: 1200px">
      <ScannerPage class="col-auto" />
      <SupercardSyncCard @synced="reloadReceipts" class="col-auto" />
    </div>

    <div class="full-width q-px-md" style="max-width: 1200px">
      <h5 class="text-center q-mt-none q-mb-md">Kassenzettel</h5>
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
            <q-card-section class="text-center q-py-sm row items-center justify-between q-px-sm">
              <div class="text-subtitle1 text-weight-bold col text-center">{{ props.row.Corp }}</div>
              <q-btn
                flat
                round
                dense
                icon="delete"
                size="sm"
                color="negative"
                class="delete-btn"
                @click="deleteReceipt(props.row)"
              />
            </q-card-section>

            <q-separator inset />

            <q-card-section class="q-pa-md">
              <div class="row justify-between text-caption text-grey-7">
                <span>Kaufdatum:</span>
                <span class="text-black text-weight-medium">{{ formatDateShort(props.row.Purchase_Date) }}</span>
              </div>
              <div v-if="props.row.Created_At" class="row justify-between text-caption text-grey-7 q-mt-xs">
                <span>Eingelesen am:</span>
                <span>{{ formatDateShort(props.row.Created_At) }}</span>
              </div>
              <div class="row justify-between items-center q-mt-sm text-body2 text-weight-bold">
                <span>Total:</span>
                <span>{{ props.row.Total_Receipt.toFixed(2) }} CHF</span>
              </div>

              <q-img
                v-if="props.row.Thumbnail"
                :src="props.row.Thumbnail"
                class="q-mt-sm rounded-borders border-grey"
                style="height: 100px"
                fit="cover"
              />
            </q-card-section>
          </q-card>
        </template>
      </q-table>

      <div class="row justify-end q-mt-md">
        <q-card class="total-card shadow-1">
          <q-card-section class="q-pa-md">
            <div class="row justify-between text-caption text-grey-7">
              <span>Kassenzettel:</span>
              <span class="text-black text-weight-medium">{{ rows.length }}</span>
            </div>
            <q-separator class="q-my-sm" />
            <div class="row justify-between items-center text-caption text-black text-weight-bold">
              <span>Total:</span>
              <span>{{ totalAmount.toFixed(2) }} CHF</span>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useQuasar } from "quasar";
import { storeToRefs } from "pinia";
import { useDataStore } from "../../stores/dataStore";
import ScannerPage from "../scanner/ScannerPage.vue";
import SupercardSyncCard from "./SupercardSyncCard.vue";
import { formatDateShort } from "../../helpers/dateHelpers";
import { deleteReceipt as deleteReceiptService } from "../../services";
import type { Receipt } from "../../helpers/interfaces/receipt.interface";

const $q = useQuasar();
const dataStore = useDataStore();
const { receipts } = storeToRefs(dataStore);

const rows = computed(() =>
  [...receipts.value].sort((a, b) => new Date(b.Purchase_Date).getTime() - new Date(a.Purchase_Date).getTime())
);

const totalAmount = computed(() => rows.value.reduce((sum, r) => sum + (r.Total_Receipt ?? 0), 0));

const reloadReceipts = async () => {
  dataStore.initialized = false;
  await dataStore.ensureInitialized();
};

const deleteReceipt = (receipt: Receipt) => {
  $q.dialog({
    title: "Löschen bestätigen",
    message: `Kassenzettel vom ${formatDateShort(receipt.Purchase_Date)} löschen?`,
    ok: { label: "Löschen", color: "negative", unelevated: true },
    cancel: { label: "Abbrechen", flat: true },
  }).onOk(async () => {
    const result = await deleteReceiptService(receipt.Id!);
    if (result.ok) dataStore.removeReceiptById(receipt.Id!);
  });
};

const columns = [{ name: "Purchase_Date", label: "Kaufdatum", field: "Purchase_Date", sortable: true }];

onMounted(async () => {
  await dataStore.ensureInitialized();
  dataStore.startRealtime();
});
</script>

<style scoped lang="scss">
.border-grey {
  border: 1px solid #ececec;
}

.delete-btn {
  opacity: 0.35;
  transition: opacity 0.2s ease;
  .receipt-card:hover & {
    opacity: 1;
  }
}

.receipt-card {
  width: 200px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.07) !important;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1) !important;
  }
}

.total-card {
  width: 240px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.07) !important;
}
</style>
