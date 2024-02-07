<template>
  <q-dialog v-model="basic" transition-show="rotate" transition-hide="rotate">
    <q-card>
      <q-card-section>
        <q-table
          flat
          bordered
          title="Esswaren auswählen"
          :rows="articles"
          :columns="columns"
          row-key="Name"
          selection="multiple"
          v-model:selected="selected"
        >
          <template v-slot:header-selection="scope">
            <q-toggle v-model="scope.selected"></q-toggle>
          </template>

          <template v-slot:body-selection="scope">
            <q-toggle v-model="scope.selected"></q-toggle>
          </template>
        </q-table>
      </q-card-section>

      <q-card-section class="q-pt-none"> </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Abbrechen" color="primary" v-close-popup></q-btn>
        <q-btn flat label="Speichern" color="primary" v-close-popup></q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script>
import { defineComponent, ref, toRefs } from "vue";

const columns = [
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
  { name: "Discount", label: "Aktion", field: "Discount" },
];

export default defineComponent({
  name: "DialogComponent",
  props: {
    // Articles
    articles: { type: Array, required: true },
  },
  setup(props) {
    const { articles } = toRefs(props);
    console.log(props.articles);

    return {
      selected: ref([]),
      basic: ref(false),
      fixed: ref(false),
      columns,
      // eslint-disable-next-line vue/no-dupe-keys
      articles,
    };
  },
});
</script>
