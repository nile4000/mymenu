<template>
  <div class="q-pa-md">
    <q-table
      :rows="rows"
      :columns="columns"
      grid
      hide-header
      title="Meine Belege"
    >
      <template v-slot:item="props">
        <div class="q-pa-xs col-xs-12 col-sm-6 col-md-4 col-lg-3">
          <q-card bordered flat>
            <q-card-section class="row items-center justify-between">
              <div class="flex-grow-1 q-mr-md text-weight-bold">
                Einkauf vom: {{ formatDate(props.row.Purchase_Date) }}
              </div>
              <q-img
                v-if="props.row.Corp === 'Coop'"
                src="../assets/coop.png"
                alt="Coop"
                style="max-width: 55px"
              />
              <q-img
                v-if="props.row.Corp === 'Migros'"
                src="../assets/migros.png"
                alt="Migros"
                style="max-width: 55px"
              />
            </q-card-section>
            <q-card-section class="row items-center justify-between" side>
              <q-btn
                flat
                icon="delete"
                color="negative"
                @click="deleteReceipt(props.row)"
                v-close-popup
              />
            </q-card-section>
          </q-card>
        </div>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { Column } from "../helpers/interfaces/column.interface";
import { defineComponent, ref, onMounted } from "vue";
import { readAllReceipts } from "../services/readAllReceipts";
import { deleteReceiptById } from "../services/deleteReceipt";
import { formatDate } from "../helpers/dateHelpers";

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
  name: "ReceiptsPage",

  setup() {
    // Define rows as an array of Receipt objects
    const rows = ref<Receipt[]>([]);
    const filter = ref<string>("");
    const selected = ref<string[]>([]);
    const allRows: Receipt[] = [];

    onMounted(async () => {
      try {
        const data = await readAllReceipts();
        if (data) {
          console.log(data);
          allRows.splice(0, allRows.length, ...data);
        }
      } catch (error) {
        console.error("Error loading articles from Supabase:", error);
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
        } catch (error) {
          console.error("Error deleting receipt:", error);
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
    };
  },
});
</script>

<style scoped lang="scss">
.custom-separator {
  margin-left: 10px;
  margin-right: 10px;
}

.custom-list {
  margin-bottom: 5px;
}

.q-card {
  transition: background-color 0.3s ease;

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
