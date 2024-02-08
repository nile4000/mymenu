<template>
  <div class="q-pa-md">
    <q-table :rows="rows" :columns="columns" grid hide-header>
      <template v-slot:item="props">
        <div class="q-pa-xs col-xs-12 col-sm-6 col-md-4 col-lg-3">
          <q-card bordered flat>
            <q-card-section class="row items-center justify-between">
              <div class="flex-grow-1 q-mr-md text-weight-bold">
                Einkauf vom: {{ props.row.PurchaseDate }}
              </div>
              <q-img
                src="../assets/coop.png"
                alt="Coop"
                style="max-width: 55px"
              />
            </q-card-section>
            <q-separator class="custom-separator"></q-separator>
            <q-list dense class="custom-list">
              <!-- just show the first 3 articles -->
              <q-item
                v-for="(article, index) in props.row.Articles.slice(0, 3)"
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
                <q-item-section side bottom>
                  <q-item-label class="text-weight-bold"
                    >Total: {{ props.row.Total }}</q-item-label
                  >
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

export default defineComponent({
  name: "ReceiptsPage",

  setup() {
    const rows = ref([]);
    const filter = ref("");
    const selected = ref([]);

    onMounted(() => {
      const allRows = [];
      Object.keys(sessionStorage).forEach((key) => {
        if (key.includes("rec")) {
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
    background-color: #3fb7935f; // Farbe nach Wahl
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
