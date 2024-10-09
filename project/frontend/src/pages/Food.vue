<template>
  <div class="q-pa-md">
    <q-table
      flat
      bordered
      title="Alle Esswaren"
      :rows="rows"
      :columns="columns"
      row-key="Name"
      selection="multiple"
      v-model="selected"
    >
      <!-- <template v-slot:top>
        <AiRequest :selectedItems="selected"></AiRequest>
      </template> -->
      <template v-slot:header-selection="scope">
        <q-toggle v-model="scope.selected"></q-toggle>
      </template>

      <template v-slot:body-selection="scope">
        <q-toggle v-model="scope.selected"></q-toggle>
      </template>
    </q-table>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, toRaw, reactive } from "vue";
// import AiRequest from "../components/AiRequest.vue";
import { Article } from "../helpers/interfaces/article.interface";
import { Column } from "../helpers/interfaces/column.interface";
import { Receipt } from "../helpers/interfaces/receipt.interface";

const columns: Column[] = [
  {
    name: "PurchaseDate",
    label: "Kaufdatum",
    field: "PurchaseDate",
    sortable: true,
    align: "center",
  },
  {
    name: "Category",
    label: "Kategorie",
    field: "Category",
    sortable: true,
    align: "center",
  },
  {
    name: "Name",
    required: true,
    label: "Name",
    align: "left",
    field: "Name",
    sortable: true,
  },
  {
    name: "Price",
    align: "center",
    label: "Preis",
    field: "Price",
    sortable: true,
  },
  { name: "Quantity", label: "Menge", field: "Quantity", sortable: true },
  { name: "Discount", label: "Rabatt", field: "Discount", sortable: true },
  { name: "Total", label: "Total", field: "Total", sortable: true },
];

export default defineComponent({
  name: "FoodPage",
  components: {
    // AiRequest,
  },

  setup() {
    const selected = ref<Article[]>([]);
    const rows = reactive<Article[]>([]);

    // Funktion, um die Daten der Tabelle zu aktualisieren
    function updateTableData() {
      const allArticles: Article[] = [];
      Object.keys(sessionStorage).forEach((key) => {
        if (key.includes("receipt")) {
          const rawValue: string | null = sessionStorage.getItem(key);
          console.log("Session Storage Wert für Schlüssel", key, ":", rawValue);
          try {
            if (rawValue) {
              // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
              const receipt: Receipt = JSON.parse(rawValue);
              if (receipt.Articles && Array.isArray(receipt.Articles)) {
                receipt.Articles.forEach((article) => {
                  article.PurchaseDate = receipt.PurchaseDate;
                  allArticles.push(article);
                });
              } else {
                console.warn("Keine Artikel in diesem Receipt:", key);
              }
            }
          } catch (e) {
            console.error(
              "Fehler beim Parsen des Session Storage Eintrags",
              key,
              e
            );
          }
        }
      });

      // Aktualisieren der reaktiven rows
      rows.splice(0, rows.length, ...allArticles);
      console.log("Aktualisierte Tabellenzeilen:", rows);
    }

    onMounted(() => {
      updateTableData();
      setInterval(updateTableData, 2000);
    });

    return {
      columns,
      rows,
      selected,
    };
  },
});
</script>
