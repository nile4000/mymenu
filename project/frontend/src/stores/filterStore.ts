import { defineStore } from "pinia";
import { ref, watch } from "vue";

const STORAGE_KEY = "mymenu_article_filters";

type StoredFilters = {
  search: string;
  selectedCategory: string | null;
  selectedReceiptIds: string[];
};

function readStoredFilters(): StoredFilters {
  if (typeof localStorage === "undefined") {
    return {
      search: "",
      selectedCategory: null,
      selectedReceiptIds: [],
    };
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return {
        search: "",
        selectedCategory: null,
        selectedReceiptIds: [],
      };
    }

    const parsed = JSON.parse(raw) as Partial<StoredFilters>;
    return {
      search: parsed.search || "",
      selectedCategory:
        typeof parsed.selectedCategory === "string" ? parsed.selectedCategory : null,
      selectedReceiptIds: Array.isArray(parsed.selectedReceiptIds)
        ? parsed.selectedReceiptIds
        : [],
    };
  } catch {
    return {
      search: "",
      selectedCategory: null,
      selectedReceiptIds: [],
    };
  }
}

export const useFilterStore = defineStore("filters", () => {
  const initial = readStoredFilters();
  const search = ref(initial.search);
  const selectedCategory = ref<string | null>(initial.selectedCategory);
  const selectedReceiptIds = ref<string[]>(initial.selectedReceiptIds);

  watch(
    [search, selectedCategory, selectedReceiptIds],
    () => {
      if (typeof localStorage === "undefined") {
        return;
      }
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          search: search.value,
          selectedCategory: selectedCategory.value,
          selectedReceiptIds: selectedReceiptIds.value,
        } satisfies StoredFilters)
      );
    },
    { deep: true }
  );

  function resetFilters() {
    search.value = "";
    selectedCategory.value = null;
    selectedReceiptIds.value = [];
  }

  return {
    search,
    selectedCategory,
    selectedReceiptIds,
    resetFilters,
  };
});
