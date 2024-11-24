<template>
  <div class="column items-center">
    <h5>Scanner</h5>
    <div class="q-pa-md row justify-evenly">
      <ScannerPage></ScannerPage>
    </div>
    <q-table
      flat
      bordered
      :rows="rows"
      :columns="columns"
      grid
      hide-header
      class="table-custom q-pa-md"
      no-data-label="Keine Daten gefunden"
      :pagination="initialPagination"
    >
      <template v-slot:top>
        <div style="width: 100%" class="row">
          <div
            class="row items-center justify-center q-gutter-sm"
            style="margin-bottom: 10px"
          >
            <q-icon size="1.4em" name="receipt_long" color="primary" />
            <span class="text-h6" style="font-weight: bold">Belege</span>
          </div>
        </div>
      </template>
      <template v-slot:item="props">
        <q-card round bordered flat>
          <q-card-section class="row items-center justify-between">
            <div class="flex-grow-1 q-mr-md text-weight-bold">
              Einkauf vom {{ formatDate(props.row.Purchase_Date) }}
            </div>
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
          </q-card-section>
          <q-card-section class="row items-center justify-between">
            <q-btn
              flat
              round
              icon="delete"
              color="negative"
              @click="deleteReceipt(props.row)"
              side
            />
          </q-card-section>
        </q-card>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { Receipt } from "../../helpers/interfaces/receipt.interface";
import { Column } from "../../helpers/interfaces/column.interface";
import { defineComponent, ref, onMounted } from "vue";
import { readAllReceipts } from "../../services/readAllReceipts";
import { deleteReceiptById } from "../../services/deleteReceipt";
import { formatDate } from "../../helpers/dateHelpers";
import { useQuasar } from "quasar";
import ScannerPage from "../Scanner.vue";
import { handleError } from "../../helpers/composables/UseErrors";

// Defining the columns
const columns: Column[] = [
  {
    name: "Purchase_Date",
    required: true,
    label: "Receipt Key",
    align: "left",
    field: "Purchase_Date",
    format: (val: string) => `${formatDate(val)}`,
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

    const initialPagination = ref({
      sortBy: "desc",
      descending: false,
      page: 1,
      rowsPerPage: 500,
    });

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
      deleteReceipt,
      initialPagination,
    };
  },
});
</script>

<style scoped lang="scss">
.table-custom {
  border-radius: 15px;
  border: 1px solid $primary;
  background-color: $bar-background;
  margin: 20px;
  margin-top: 10px;
}
.custom-separator {
  margin-left: 10px;
  margin-right: 10px;
}

.custom-list {
  margin-bottom: 5px;
}

h5 {
  margin-block-end: 0px;
}

.q-card {
  max-width: 280px;
  margin-left: 10px;
  margin-right: 10px;
  transition: background-color 0.3s ease;
  border-radius: 15px;
  background-color: white;

  &:hover {
    cursor: pointer;
    border-color: $positive;
    box-shadow: 0px 6px 6px rgba(0, 0, 0, 0.5);
    .custom-separator {
      background-color: $positive;
    }
    .q-item:last-child {
      .q-item__label {
        color: black;
      }
    }
  }
}

.q-item {
  &:not(:last-child) {
    .q-item__label {
      font-style: italic;
    }
  }
}
</style>
