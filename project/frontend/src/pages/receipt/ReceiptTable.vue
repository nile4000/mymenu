<template>
  <q-page class="column items-center q-pa-md bg-grey-1">
    <div class="text-h5 q-mb-md">Scanner</div>
    <ScannerPage class="q-mb-xl" />

    <SupercardSyncCard @synced="reloadReceipts" />

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
                <span>Kaufdatum:</span>
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

      <div class="row justify-end q-mt-md">
        <q-card class="total-card shadow-1">
          <q-card-section class="q-pa-md">
            <div class="row justify-between text-caption text-grey-7">
              <span>Kassenzettel:</span>
              <span class="text-black text-weight-medium">{{ rows.length }}</span>
            </div>
            <q-separator class="q-my-sm" />
            <div class="row justify-between items-center">
              <span class="text-caption text-grey-7">Total:</span>
              <span class="text-subtitle1 text-bold">{{ totalAmount.toFixed(2) }} CHF</span>
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
const { receipts: rows } = storeToRefs(dataStore);

const totalAmount = computed(() =>
  rows.value.reduce((sum, r) => sum + (r.Total_Receipt ?? 0), 0)
);

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

const columns = [{ name: "Purchase_Date", field: "Purchase_Date", sortable: true }];

onMounted(async () => {
  await dataStore.ensureInitialized();
  dataStore.startRealtime();
});
</script>

<style scoped lang="scss">
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

.total-card {
  width: 240px;
  border-radius: 12px;
  margin-left: auto;
  background-color: #fafafa;
}

</style>
