<template>
  <div style="width: 100%">
    <q-input
      rounded
      dense
      debounce="400"
      :model-value="modelValue"
      @update:model-value="$emit('update:modelValue', $event)"
      outlined
      label="Kaufdatum, Name oder Kategorie"
    >
      <template #append>
        <q-icon name="search" />
      </template>
    </q-input>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "TableSearchInput",
  props: {
    modelValue: {
      type: String,
      required: true,
    },
  },
  emits: ["update:modelValue"],
});
</script>
