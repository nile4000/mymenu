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
      <template v-slot:top>
        <AiRequest :selectedItems="selected"></AiRequest>
      </template>
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
import AiRequest from "../components/AiRequest.vue";
import { Article } from "../helpers/interfaces/article.interface";
import { Column } from "../helpers/interfaces/column.interface";

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
    name: "Price",
    align: "center",
    label: "Preis",
    field: "Price",
    sortable: true,
  },
  { name: "Quantity", label: "Menge", field: "Quantity", sortable: true },
];

export default defineComponent({
  name: "FoodPage",
  components: {
    AiRequest,
  },

  setup() {
    const selected = ref([]);
    const rows = reactive([]); // Make rows reactive

    // Funktion, um die Daten der Tabelle zu aktualisieren
    function updateTableData() {
      const allArticles: Article[] = [];
      Object.keys(sessionStorage).forEach((key) => {
        if (key.includes("food")) {
          const rawValue = sessionStorage.getItem(key);
          try {
            if (rawValue) {
              const itemValue: Article[] = JSON.parse(rawValue);
              itemValue.forEach((article) => allArticles.push(article));
            }
          } catch (e) {
            console.error("Error parsing session storage item", key, e);
          }
        }
      });

      Object.assign(rows, toRaw(allArticles));
    }

    onMounted(() => {
      updateTableData(); // initial ausführen
      setInterval(updateTableData, 1000); // alle 1 Sekunde wiederholen
    });

    return {
      columns,
      rows,
      selected,
    };
  },
});
</script>
