<template>
  <div class="q-pa-md">
    <q-table
      :rows="rows"
      :columns="columns"
      row-key="UID"
      selection="single"
      v-model:selected="selected"
      :filter="filter"
      grid
      hide-header
    >
      <template v-slot:item="props">
        <div
          class="q-pa-xs col-xs-12 col-sm-6 col-md-4 col-lg-3 grid-style-transition"
          :style="props.selected ? 'transform: scale(0.95);' : ''"
        >
          <q-card
            bordered
            flat
            :class="
              props.selected
                ? $q.dark.isActive
                  ? 'bg-grey-9'
                  : 'bg-grey-2'
                : ''
            "
          >
            <q-card-section class="row items-center justify-between">
              <q-checkbox
                dense
                v-model="props.selected"
                class="q-mr-md"
              ></q-checkbox>
              <div class="flex-grow-1 text-center q-mr-md">
                {{ props.row.PurchaseDate }}
              </div>
              <div class="text-weight-bold q-mr-md">
                {{ props.row.Total }}.-
              </div>
              <q-img
                src="../assets/coop.png"
                alt="Coop"
                style="max-width: 55px"
                class="q-mr-md"
              />
            </q-card-section>
            <q-separator class="custom-separator"></q-separator>
            <q-list dense class="custom-list">
              <q-item
                v-for="(article, index) in props.row.Articles"
                :key="index"
              >
                <q-item-section>
                  <q-item-label>{{ article.Name }}</q-item-label>
                </q-item-section>
              </q-item>
              <q-item>
                <q-item-section>
                  <q-item-label>...</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-card>
        </div>
      </template>
    </q-table>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from "vue";

export default defineComponent({
  name: "ReceiptsPage",

  setup() {
    const rows = ref([]);
    const filter = ref("");
    const selected = ref([]);

    const columns = [
      {
        name: "PurchaseDate",
        required: true,
        label: "Receipt Key",
        align: "left",
        field: "PurchaseDate",
        sortable: true,
      },
    ];

    onMounted(() => {
      const allRows = [];
      Object.keys(sessionStorage).forEach((key) => {
        try {
          const itemValue = JSON.parse(sessionStorage.getItem(key));
          const receipt = itemValue;
          allRows.push({
            Total: receipt.Total,
            PurchaseDate: receipt.PurchaseDate,
            Articles: receipt.Articles,
          });
        } catch (e) {
          console.error("Error parsing session storage item", key, e);
        }
      });
      rows.value = allRows;
    });

    return {
      filter,
      selected,
      columns,
      rows,
    };
  },
});
</script>

<style scoped lang="scss">
.grid-style-transition {
  transition: transform 0.28s, background-color 0.28s;
}

.custom-separator {
  margin-left: 10px;
  margin-right: 10px;
}

.custom-list {
  margin-bottom: 5px;
}
</style>
