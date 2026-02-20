<template>
  <q-expansion-item
    :dense="dense"
    :class="panelClass"
    :caption="caption"
    :default-opened="defaultOpened"
    :color="color"
  >
    <template v-slot:header>
      <q-item-section avatar>
        <q-avatar :icon="icon" :class="iconClass" />
      </q-item-section>
      <q-item-section style="font-weight: bold">
        {{ title }}
      </q-item-section>
      <q-item-section side v-if="badgeText">
        <q-badge color="primary" text-color="white">
          {{ badgeText }}
        </q-badge>
      </q-item-section>
    </template>

    <slot name="controls" />

    <q-scroll-area :class="scrollClass" :style="{ height: scrollHeight }">
      <slot />
    </q-scroll-area>
  </q-expansion-item>
</template>

<script lang="ts">
import { defineComponent } from "vue";

export default defineComponent({
  name: "FilterPanel",
  props: {
    title: {
      type: String,
      default: "",
    },
    caption: {
      type: String,
      default: "",
    },
    icon: {
      type: String,
      default: "",
    },
    iconClass: {
      type: String,
      default: "",
    },
    badgeText: {
      type: String,
      default: "",
    },
    panelClass: {
      type: String,
      default: "",
    },
    scrollClass: {
      type: String,
      default: "filter-scroll",
    },
    scrollHeight: {
      type: String,
      default: "240px",
    },
    color: {
      type: String,
      default: "",
    },
    defaultOpened: {
      type: Boolean,
      default: false,
    },
    dense: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
