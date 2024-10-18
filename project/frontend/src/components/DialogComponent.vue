<template>
  <q-dialog transition-show="rotate">
    <q-card>
      <q-card-section class="q-gutter-md">
        <q-row>
          <q-col cols="6" class="flex flex-center">
            <div class="ml-2">
              <div class="text-subtitle1 text-green-5">
                Total Zeilen Extrahiert: {{ receiptData.Total_R_Extract }}
              </div>
            </div>
          </q-col>
          <q-col cols="6" class="flex flex-center">
            <div class="ml-2">
              <div class="text-subtitle1 text-blue-5">
                Total Zeilen von OpenAI: {{ receiptData.Total_R_Open_Ai }}
              </div>
            </div>
          </q-col>
        </q-row>
      </q-card-section>
      <q-card-section>
        <q-table
          flat
          bordered
          title="Esswaren auswählen"
          :rows="articles"
          :columns="columns"
          row-key="Name"
          no-data-label="Keine Daten gefunden"
        >
        </q-table>
      </q-card-section>
      <q-card-section class="q-pt-none"> </q-card-section>
      <q-card-actions align="right">
        <q-btn flat label="Abbrechen" color="primary" v-close-popup></q-btn>
        <q-btn
          flat
          label="Speichern"
          color="primary"
          v-close-popup
          @click="saveAll"
        ></q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, computed, PropType } from "vue";
import { Column } from "../helpers/interfaces/column.interface";
import { saveArticlesAndReceipt } from "../services/saveArticles";
import { ResponseItem } from "../helpers/interfaces/response-item.interface";

const columns: Column[] = [
  {
    name: "Name",
    required: true,
    label: "Name",
    align: "left",
    field: "Name",
    sortable: true,
  },
  {
    name: "Preis",
    align: "center",
    label: "Preis",
    field: "Price",
    sortable: true,
  },
  { name: "Menge", label: "Menge", field: "Quantity", sortable: true },
  { name: "Rabatt", label: "Aktion", field: "Discount", sortable: true },
  { name: "Total", label: "Gesamt", field: "Total", sortable: true },
  { name: "Kategorie", label: "Kategorie", field: "Category", sortable: true },
];

export default defineComponent({
  props: {
    response: {
      type: Array as PropType<ResponseItem[]>,
      required: true,
    },
  },
  setup(props, { emit }) {
    const selected = ref([]);
    const articles = computed(() => props.response[0].Articles);

    const receiptData = computed(() => ({
      Uuid: props.response[0].UID,
      Purchase_Date: props.response[0].Purchase_Date,
      Corp: props.response[0].Corp,
      Total_R_Extract: props.response[0].Total_R_Extract,
      Total_R_Open_Ai: props.response[0].Total_R_Open_Ai,
      Total_Receipt: parseFloat(props.response[0].Total),
    }));

    const saveAll = async () => {
      try {
        emit("save-selection");
        await saveArticlesAndReceipt(articles.value, receiptData.value);
      } catch (error: any) {
        console.error("Error saving selection:", error);
      }
    };

    return {
      selected,
      articles,
      saveAll,
      columns,
      receiptData,
    };
  },
});
</script>

<style scoped lang="scss">
.q-table__card .q-table__top,
.q-table__card .q-table__bottom {
  background-color: #3fb7935f;
}

.ml-2 {
  margin-left: 0.5rem; /* Passe den Wert nach Bedarf an */
}

.text-subtitle1 {
  font-weight: 500;
}

.text-h6 {
  font-weight: 600;
}

.text-primary {
  color: #027be3; /* Passe die Farbe nach deinem Design an */
}

.text-green-5 {
  color: #4caf50; /* Grün für Total Extrahiert */
}

.text-blue-5 {
  color: #2196f3; /* Blau für Total OpenAI */
}
</style>
