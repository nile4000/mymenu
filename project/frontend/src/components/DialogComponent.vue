<template>
  <q-dialog transition-show="rotate">
    <q-card>
      <q-card-section>
        <q-table
          flat
          bordered
          title="Esswaren auswählen"
          :rows="articles"
          :columns="columns"
          row-key="Name"
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
import { Article } from "../helpers/interfaces/article.interface";
import { defineComponent, ref, computed, PropType, toRaw } from "vue";
import { Column } from "../helpers/interfaces/column.interface";
import { saveArticles } from "../services/saveArticles";

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

interface ResponseItem {
  UID: string;
  Articles: Article[];
}

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

    const saveAll = async () => {
      try {
        emit("save-selection");
        await saveArticles(articles.value);
      } catch (error: any) {
        console.error("Error saving selection:", error);
      }
    };

    return {
      selected,
      articles,
      saveAll,
      columns,
      // saveSelection,
    };
  },
});
</script>
<style scoped lang="scss">
.q-table__card .q-table__top,
.q-table__card .q-table__bottom {
  background-color: #3fb7935f;
}
</style>
