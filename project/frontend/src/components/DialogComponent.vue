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
          v-model="selected"
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
        <q-btn
          flat
          label="Speichern"
          color="primary"
          v-close-popup
          @click="saveSelection"
        ></q-btn>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, computed, toRaw, PropType } from "vue";

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

interface ResponseItem {
  UID: string;
  Articles: any[];
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

    const saveSelection = () => {
      return new Promise<void>((resolve, reject) => {
        try {
          emit("save-selection");
          const uid = props.response[0].UID || "";
          sessionStorage.setItem(
            "food_" + uid,
            JSON.stringify(toRaw(selected.value))
          );
          resolve();
        } catch (error) {
          reject(error);
        }
      }).catch((error) => {
        console.error("Error saving selection:", error);
      });
    };

    return {
      selected,
      articles,
      saveSelection,
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
