<template>
  <q-list class="q-gutter-md" style="display: flex; flex-direction: row">
    <q-expansion-item
      label="Total pro Kategorie"
      icon="category"
      expand-separator
      expand-icon="0"
      :dense="false"
      class="my-card"
    >
      <q-list>
        <q-item v-for="(total, category) in totalsPerCategory" :key="category">
          <q-item-section>
            {{ category }}
          </q-item-section>
          <q-item-section side
            ><div class="text-body1">
              {{ total.toFixed(2) }}.-
            </div></q-item-section
          >
        </q-item>
      </q-list>
    </q-expansion-item>
    <q-expansion-item
      label="Total pro Beleg"
      icon="receipt_long"
      expand-separator
      expand-icon="0"
      :dense="false"
      default-opened
      class="my-card"
      caption="Total CHF (eingescannt)."
    >
      <q-scroll-area style="height: 100px">
        <q-list>
          <q-item v-for="receipt in totalsPerReceipt" :key="receipt.id">
            <q-item-section>
              {{ formatDate(receipt.date) }}
            </q-item-section>
            <q-item-section>
              <div class="text-body1">{{ receipt.total.toFixed(2) }}.-</div>
            </q-item-section>
            <q-item-section side>
              <q-icon
                :name="
                  selectedReceipts[receipt.id] ? 'visibility' : 'visibility_off'
                "
                @click="
                  toggleReceiptSelection(
                    receipt.id,
                    !selectedReceipts[receipt.id]
                  )
                "
                class="cursor-pointer"
              />
            </q-item-section>
          </q-item>
        </q-list>
      </q-scroll-area>
    </q-expansion-item>
  </q-list>
</template>

<script lang="ts">
import { defineComponent, PropType, reactive, watch } from "vue";
import { formatDate } from "../helpers/dateHelpers";

export default defineComponent({
  name: "FoodTotal",
  props: {
    totalsPerCategory: {
      type: Object as () => Record<string, number>,
      required: true,
    },
    totalsPerReceipt: {
      type: Array as PropType<
        Array<{ id: string; date: string; total: number }>
      >,
      required: true,
    },
    totalCalculatedPerReceipt: {
      type: Object as PropType<Record<string, { total: number; date: string }>>,
      required: true,
    },
  },
  emits: ["update:selectedReceipts"],
  setup(props, { emit }) {
    const selectedReceipts = reactive<Record<string, boolean>>({});

    props.totalsPerReceipt.forEach((receipt) => {
      selectedReceipts[receipt.id] = true;
    });

    const toggleReceiptSelection = (id: string, value: boolean) => {
      selectedReceipts[id] = value;
    };

    watch(
      () => ({ ...selectedReceipts }),
      (newVal) => {
        const selectedIds = Object.keys(newVal).filter((id) => newVal[id]);
        emit("update:selectedReceipts", selectedIds);
      },
      { deep: true, immediate: false }
    );

    return {
      selectedReceipts,
      toggleReceiptSelection,
      formatDate,
    };
  },
});
</script>

<style lang="scss" scoped>
.my-card {
  height: fit-content;
  border-radius: 15px;
  border: 1px solid $primary;
}
.q-item:focus {
  background-color: none;
  border-radius: 15px;
}

:deep(.q-expansion-item) {
  .q-focus-helper {
    border-radius: 15px;
  }
}

.text-h6 {
  font-weight: bold;
}

.q-pa-md {
  padding: 16px;
}
</style>
