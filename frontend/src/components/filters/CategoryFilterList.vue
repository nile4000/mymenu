<template>
  <q-list>
    <q-item
      v-if="showAllOption"
      clickable
      :class="{ 'active-filter-item': modelValue === null }"
      @click="emitValue(null)"
    >
      <q-item-section>{{ allLabel }}</q-item-section>
      <q-item-section side>
        <q-radio :model-value="modelValue" :val="null" color="primary" />
      </q-item-section>
    </q-item>

    <q-item
      v-for="item in options"
      :key="item.value"
      clickable
      :class="{ 'active-filter-item': modelValue === item.value }"
      @click="emitValue(item.value)"
    >
      <q-item-section v-if="item.icon">
        <q-icon size="sm" :name="item.icon" :color="item.iconColor || 'primary'" />
      </q-item-section>
      <q-item-section :style="item.icon ? 'margin-left: 18px' : ''">
        <div :style="{ 'font-weight': modelValue === item.value ? 'bold' : 'normal' }">
          {{ item.label }}
        </div>
      </q-item-section>
      <q-item-section side v-if="typeof item.amount === 'number'">
        <div class="text-body1">{{ item.amount.toFixed(2) }} CHF</div>
      </q-item-section>
      <q-item-section side v-else>
        <q-radio :model-value="modelValue" :val="item.value" color="primary" />
      </q-item-section>
    </q-item>

    <q-item v-if="options.length === 0">
      <q-item-section>{{ emptyText }}</q-item-section>
    </q-item>
  </q-list>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";

type CategoryFilterItem = {
  value: string;
  label: string;
  icon?: string;
  iconColor?: string;
  amount?: number;
};

export default defineComponent({
  name: "CategoryFilterList",
  props: {
    modelValue: {
      type: String as PropType<string | null>,
      default: null,
    },
    options: {
      type: Array as PropType<CategoryFilterItem[]>,
      default: () => [],
    },
    showAllOption: {
      type: Boolean,
      default: false,
    },
    allLabel: {
      type: String,
      default: "Alle Kategorien",
    },
    emptyText: {
      type: String,
      default: "Keine Kategorien verfuegbar",
    },
  },
  emits: ["update:modelValue"],
  setup(_, { emit }) {
    function emitValue(value: string | null) {
      emit("update:modelValue", value);
    }

    return {
      emitValue,
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
