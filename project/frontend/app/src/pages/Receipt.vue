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

<script lang="ts">
import { Receipt } from "../helpers/interfaces/receipt.interface";
import { Column } from "../helpers/interfaces/column.interface";
import { defineComponent, ref, onMounted } from "vue";

// Defining the columns
const columns: Column[] = [
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
    // Define rows as an array of Receipt objects
    const rows = ref<Receipt[]>([]);
    const filter = ref<string>("");
    const selected = ref<string[]>([]);

    onMounted(() => {
      const allRows: Receipt[] = [];
      Object.keys(sessionStorage).forEach((key) => {
        if (key.includes("receipt")) {
          try {
            const itemValue = sessionStorage.getItem(key);
            if (itemValue) {
              const receipt: Receipt = JSON.parse(itemValue);
              allRows.push(receipt);
            }
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
