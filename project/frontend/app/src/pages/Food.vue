<template>
  <div class="q-pa-md">
    <q-table
      :rows="rows"
      :columns="columns"
      row-key="Name"
      selection="multiple"
      v-model:selected="selected"
      :filter="filter"
      grid
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
  name: "FoodPage",

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
        if (key.includes("edb")) {
          try {
            const itemValue = JSON.parse(sessionStorage.getItem(key));
            const articles = itemValue;
            allRows.push({
              Articles: articles,
            });
          } catch (e) {
            console.error("Error parsing session storage item", key, e);
          }
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
