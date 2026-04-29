<template>
  <div class="supercard-card shadow-2 q-pa-md">
    <div class="row items-center justify-between q-mb-md">
      <div class="column">
        <span class="text-subtitle1 text-bold text-primary">Supercard Sync</span>
        <span class="text-caption text-grey-7">Automatischer Beleg-Import</span>
      </div>
      <q-chip dense square :color="session.connected ? 'positive' : 'orange'" text-color="white" class="text-weight-bold">
        {{ session.connected ? "Verbunden" : "Session fehlt" }}
      </q-chip>
    </div>

    <div class="step-container q-mb-sm">
      <div class="row items-center no-wrap q-gutter-sm">
        <div class="step-badge">1</div>
        <div class="col">
          <div class="text-weight-bold">Zugangsdaten</div>
          <div class="text-caption text-grey-8 truncate-ts">
            {{
              session.connected
                ? `Aktiv seit: ${formatDateTimeCh(session.updatedAt)}`
                : "Cookie aus DevTools einfügen"
            }}
          </div>
        </div>
        <q-btn
          flat
          dense
          color="primary"
          :icon="cookieForm.visible ? 'expand_less' : 'edit'"
          @click="cookieForm.visible = !cookieForm.visible"
        />
      </div>

      <q-slide-transition>
        <div v-if="cookieForm.visible || !session.connected" class="q-mt-sm">
          <q-input
            v-model="cookieForm.value"
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
              :loading="requests.saveSession"
              @click="connectSupercard"
            />
            <q-btn flat dense color="grey-7" icon="delete_sweep" @click="resetSupercardDraft" />
          </div>
        </div>
      </q-slide-transition>
    </div>

    <div v-if="session.connected" class="step-container q-mb-sm">
      <div class="row items-center no-wrap q-gutter-sm">
        <div class="step-badge">2</div>
        <div class="col">
          <div class="text-weight-bold">Vorschau</div>
          <div class="text-caption text-grey-8">{{ availableReceiptsCount }} neue Belege gefunden</div>
        </div>
        <q-btn flat round dense icon="refresh" color="secondary" :loading="requests.loadPreview" @click="loadAvailableReceipts" />
      </div>

      <q-scroll-area
        v-if="preview.loaded && preview.receipts.length"
        style="height: 180px"
        class="q-mt-sm border-grey rounded-borders bg-white"
      >
        <q-list dense separator>
          <q-item v-for="receipt in preview.receipts" :key="receipt.supercardReceiptBarcode">
            <q-item-section avatar>
              <q-img :src="receipt.logoUrl || ''" width="28px" height="28px" fit="contain" />
            </q-item-section>
            <q-item-section>
              <q-item-label class="text-caption text-weight-medium">{{ receipt.locationName }}</q-item-label>
              <q-item-label caption>{{ formatOptionalDateShort(receipt.purchaseDate) }}</q-item-label>
            </q-item-section>
            <q-item-section side class="text-black text-weight-bold">
              {{ formatChfAmount(receipt.totalChf) }}
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
        :loading="requests.syncReceipts"
        @click="runSupercardSync"
      >
        <q-icon name="cloud_download" class="q-mr-sm" />
        {{ availableReceiptsCount }} Belege importieren
      </q-btn>
    </div>

    <q-banner
      v-if="sync.summary"
      dense
      rounded
      class="q-mt-sm text-caption"
      :class="sync.summaryTone === 'warning' ? 'bg-warning text-dark' : 'bg-positive text-white'"
    >
      <div class="text-center">{{ sync.summary }}</div>
      <div v-if="sync.errors.length" class="q-mt-xs">
        <div v-for="error in sync.errors.slice(0, 2)" :key="error" class="ellipsis">
          {{ error }}
        </div>
      </div>
    </q-banner>
  </div>
</template>

<script setup lang="ts">
import { formatDateTimeCh, formatOptionalDateShort } from "../../helpers/dateHelpers";
import { formatChfAmount } from "../../helpers/numberHelpers";
import { useSupercardSync } from "./composables/useSupercardSync";

// reload receipts events after successful supercard sync
const emit = defineEmits<{
  (event: "synced"): void;
}>();

const {
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
} = useSupercardSync({
  onSynced: () => emit("synced"),
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

.truncate-ts {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
