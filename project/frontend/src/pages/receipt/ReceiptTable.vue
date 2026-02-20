<template>
  <div class="column items-center">
    <h5>Scanner</h5>
    <div class="q-pa-md row justify-evenly">
      <ScannerPage></ScannerPage>
    </div>
    <h5 style="margin-block-start: 15px">Meine Kassenzettel</h5>
    <q-table
      flat
      bordered
      grid
      :rows="rows"
      :columns="columns"
      hide-header
      hide-bottom
      no-data-label="Keine Daten gefunden"
      :pagination="initialPagination"
    >
      <template v-slot:item="props">
        <q-card class="receipt-card">
          <!-- Logos -->
          <div class="q-pa-md row justify-center">
            <q-img
              v-if="props.row.Corp === 'Coop'"
              src="../../assets/coop.png"
              alt="Coop"
              style="max-width: 55px"
            />
            <q-img
              v-if="props.row.Corp === 'Migros'"
              src="../../assets/migros.png"
              alt="Migros"
              style="max-width: 55px"
            />
          </div>
          <!-- PDF Icon -->
          <q-card-section class="q-pa-sm">
            <div class="q-gutter-md q-pa-xs column">
              <div class="row items-center justify-between">
                <span class="text-subtitle2">
                  Eingekauft am: {{ formatDateShort(props.row.Purchase_Date) }}
                </span>
              </div>
              <div class="row items-center justify-between">
                <span class="text-subtitle2">
                  Gescannt am: {{ formatDateShort(props.row.Created_At) }}
                </span>
              </div>

              <div class="row items-center justify-between">
                <span class="text-subtitle2">
                  Total CHF: {{ props.row.Total_Receipt.toFixed(2) }}
                </span>
              </div>
            </div>
          </q-card-section>
          <q-card-section>
            <q-img
              :src="props.row.Thumbnail"
              alt="PDF Vorschau"
              style="max-width: 100%; max-height: 150px"
              v-if="props.row.Thumbnail"
            />
          </q-card-section>
          <!-- Actions -->
          <q-card-section class="row items-center justify-center">
            <q-btn
              flat
              round
              disabled
              icon="visibility"
              ><q-tooltip anchor="center left" class="text-h5"
                >PDF anzeigen</q-tooltip
              ></q-btn
            >
            <q-btn flat round icon="delete" @click="deleteReceipt(props.row)">
              <q-tooltip anchor="center left" class="text-h5"
                >Löschen</q-tooltip
              >
            </q-btn>
          </q-card-section>
        </q-card>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { RealtimeChannel } from "@supabase/supabase-js";
import { useQuasar } from "quasar";
import { defineComponent, onMounted, onUnmounted, ref } from "vue";
import { handleError } from "../../helpers/composables/UseErrors";
import { formatDate, formatDateShort } from "../../helpers/dateHelpers";
import { Column } from "../../helpers/interfaces/column.interface";
import { Receipt } from "../../helpers/interfaces/receipt.interface";
import { deleteReceiptById } from "../../services/deleteReceipt";
import { readAllReceipts } from "../../services/readAllReceipts";
import {
  subscribeToReceiptChanges,
  unsubscribeFromReceiptChanges,
} from "../../services/realtimeReceipts";
import ScannerPage from "../Scanner.vue";

// Defining the columns
const columns: Column[] = [
  {
    name: "Purchase_Date",
    required: true,
    label: "Receipt Key",
    align: "left",
    field: "Purchase_Date",
    format: (val: string) => `${formatDateShort(val)}`,
    sortable: true,
  },
];

export default defineComponent({
  name: "ReceiptTable",
  components: {
    ScannerPage,
  },

  setup() {
    const $q = useQuasar();
    const rows = ref<Receipt[]>([]);
    const filter = ref<string>("");
    const selected = ref<string[]>([]);
    const allRows: Receipt[] = [];

    let channel: RealtimeChannel;

    const initialPagination = ref({
      sortBy: "desc",
      descending: false,
      page: 1,
      rowsPerPage: 500,
    });

    const handleReceiptChange = (payload: any) => {
      const newReceipt = payload.new as Receipt;
      const eventType = payload.eventType;

      switch (eventType) {
        case "INSERT":
          rows.value.push(newReceipt);
          break;
        case "DELETE":
          // eslint-disable-next-line no-case-declarations
          const indexToDelete = rows.value.findIndex(
            (receipt: Receipt) => receipt.Id === newReceipt.Id
          );
          if (indexToDelete !== -1) {
            rows.value.splice(indexToDelete, 1);
          }
          break;
        default:
          $q.notify({
            type: "warning",
            message: `Unknown event type: ${eventType}`,
          });
      }
    };

    onMounted(async () => {
      try {
        const data = await readAllReceipts();
        if (data) {
          allRows.splice(0, allRows.length, ...data);
        }
      } catch (error) {
        handleError("Belege laden", error, $q);
      }
      rows.value = allRows;
      channel = subscribeToReceiptChanges(handleReceiptChange);
    });

    onUnmounted(() => {
      if (channel) {
        unsubscribeFromReceiptChanges(channel);
      }
    });


    const deleteReceipt = async (receipt: Receipt) => {
      const confirmed = confirm(
        `Sind sich sicher, dass Sie den Einkauf vom ${receipt.Purchase_Date} löschen wollen?`
      );
      if (confirmed && receipt.Id) {
        try {
          await deleteReceiptById(receipt.Id);
          rows.value = rows.value.filter((row) => row.Id !== receipt.Id);
          $q.notify({
            type: "positive",
            message: "Beleg gelöscht.",
          });
        } catch (error) {
          handleError("Beleg löschen", error, $q);
        }
      }
    };

    return {
      filter,
      selected,
      columns,
      rows,
      formatDate,
      formatDateShort,
      deleteReceipt,
      initialPagination,
    };
  },
});
</script>

<style scoped lang="scss">
:deep(.q-table--grid) .q-table__grid-content {
  justify-content: center;
}

.receipt-card {
  position: relative;
  width: 220px;
  margin: 10px;
  transition: background-color 0.3s ease;
  background-color: $bar-background;
  border: none;
  overflow: hidden;

  .q-img {
    margin-top: 25px;
  }

  --clip-path: polygon(
    0% 5%,
    5% 0%,
    10% 5%,
    15% 0%,
    20% 5%,
    25% 0%,
    30% 5%,
    35% 0%,
    40% 5%,
    45% 0%,
    50% 5%,
    55% 0%,
    60% 5%,
    65% 0%,
    70% 5%,
    75% 0%,
    80% 5%,
    85% 0%,
    90% 5%,
    95% 0%,
    100% 5%,
    100% 95%,
    95% 100%,
    90% 95%,
    85% 100%,
    80% 95%,
    75% 100%,
    70% 95%,
    65% 100%,
    60% 95%,
    55% 100%,
    50% 95%,
    45% 100%,
    40% 95%,
    35% 100%,
    30% 95%,
    25% 100%,
    20% 95%,
    15% 100%,
    10% 95%,
    5% 100%,
    0% 95%
  );

  clip-path: var(--clip-path);

  &:hover {
    box-shadow: 0px 6px 6px rgba(0, 0, 0, 0.5);
    .custom-separator {
      background-color: $dark;
    }
    .q-item:last-child {
      .q-item__label {
        color: black;
      }
    }
  }
}

.q-btn {
  :deep(.q-icon) {
    text-decoration: underline;
    text-decoration-color: $dark;
    transition: text-decoration-color 0.5s ease;
    text-underline-offset: 4px;
    text-decoration-thickness: 1.5px;
    &:hover {
      background-color: none;
      text-decoration: underline;
      text-decoration-color: $negative;
    }
  }
  :deep(.q-focus-helper) {
    display: none;
  }
}

.receipt-card .q-icon {
  margin-right: 10px;
}

.custom-list {
  margin-bottom: 5px;
}

h5 {
  margin-block-start: 25px;
  margin-block-end: 0px;
}

.q-item {
  &:not(:last-child) {
    .q-item__label {
      font-style: italic;
    }
  }
}
</style>
