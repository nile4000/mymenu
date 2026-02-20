<template>
  <div>
    <div v-if="showBulkActions" class="row q-px-md q-pb-sm q-gutter-sm">
      <q-btn size="sm" flat color="primary" label="Alle" @click="selectAll" />
      <q-btn size="sm" flat color="negative" label="Keine" @click="clearAll" />
    </div>

    <q-list>
      <q-item
        v-for="item in options"
        :key="item.id"
        clickable
        :class="{ 'active-filter-item': isSelected(item.id) }"
        @click="toggle(item.id)"
      >
        <q-item-section>{{ item.label }}</q-item-section>
        <q-item-section v-if="typeof item.amount === 'number'">
          <div class="text-body1">{{ item.amount.toFixed(2) }} CHF</div>
        </q-item-section>
        <q-item-section side>
          <q-icon
            v-if="showVisibilityIcon"
            :name="isSelected(item.id) ? 'visibility' : 'visibility_off'"
            class="cursor-pointer"
            @click.stop="toggle(item.id)"
          >
            <q-tooltip v-if="tooltipText" anchor="center left" class="text-h6">
              {{ tooltipText }}
            </q-tooltip>
          </q-icon>
          <q-checkbox
            v-else
            :model-value="isSelected(item.id)"
            color="primary"
            @update:model-value="setSelected(item.id, Boolean($event))"
          />
        </q-item-section>
      </q-item>

      <q-item v-if="options.length === 0">
        <q-item-section>{{ emptyText }}</q-item-section>
      </q-item>
    </q-list>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, PropType } from "vue";

type ReceiptFilterItem = {
  id: string;
  label: string;
  amount?: number;
};

export default defineComponent({
  name: "ReceiptFilterList",
  props: {
    modelValue: {
      type: Array as PropType<string[]>,
      default: () => [],
    },
    options: {
      type: Array as PropType<ReceiptFilterItem[]>,
      default: () => [],
    },
    emptyText: {
      type: String,
      default: "Keine Kassenzettel verfuegbar",
    },
    showBulkActions: {
      type: Boolean,
      default: false,
    },
    showVisibilityIcon: {
      type: Boolean,
      default: false,
    },
    tooltipText: {
      type: String,
      default: "",
    },
  },
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    const selectedIds = computed<string[]>({
      get: () => (Array.isArray(props.modelValue) ? props.modelValue : []),
      set: (value) => emit("update:modelValue", value),
    });

    function isSelected(id: string): boolean {
      return selectedIds.value.map((item) => String(item)).includes(String(id));
    }

    function toggle(id: string) {
      const normalizedId = String(id);
      if (isSelected(normalizedId)) {
        selectedIds.value = selectedIds.value
          .map((item) => String(item))
          .filter((item) => item !== normalizedId);
      } else {
        selectedIds.value = [...selectedIds.value.map((item) => String(item)), normalizedId];
      }
    }

    function setSelected(id: string, selected: boolean) {
      const normalizedId = String(id);
      if (selected && !isSelected(normalizedId)) {
        selectedIds.value = [...selectedIds.value.map((item) => String(item)), normalizedId];
        return;
      }
      if (!selected && isSelected(normalizedId)) {
        selectedIds.value = selectedIds.value
          .map((item) => String(item))
          .filter((item) => item !== normalizedId);
      }
    }

    function selectAll() {
      selectedIds.value = props.options.map((item) => String(item.id));
    }

    function clearAll() {
      selectedIds.value = [];
    }

    return {
      isSelected,
      toggle,
      setSelected,
      selectAll,
      clearAll,
    };
  },
});
</script>

<style scoped lang="scss">
.active-filter-item {
  background-color: rgba(34, 46, 87, 0.08);
  border-left: 3px solid $primary;
}
</style>
