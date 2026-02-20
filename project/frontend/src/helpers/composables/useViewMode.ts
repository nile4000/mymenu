import { ref } from "vue";

export function useViewMode() {
  const isGridView = ref(false);
  function toggleView() {
    isGridView.value = !isGridView.value;
  }

  return { isGridView, toggleView };
}
