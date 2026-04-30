import { defineStore } from "pinia";
import { ref, watch } from "vue";

const STORAGE_KEY = "mymenu_article_filters";

export type ArticleQuickFilter =
  | "all"
  | "articles"
  | "adjustments"
  | "negative"
  | "uncategorized"
  | "noUnit";

type StoredFilters = {
  search: string;
  selectedCategory: string | null;
  selectedReceiptIds: string[];
  quickFilter: ArticleQuickFilter;
};

function parseQuickFilter(value: unknown): ArticleQuickFilter {
  return ["articles", "adjustments", "negative", "uncategorized", "noUnit"].includes(String(value))
    ? (value as ArticleQuickFilter)
    : "all";
}

function readStoredFilters(): StoredFilters {
  if (typeof localStorage === "undefined") {
    return {
      search: "",
      selectedCategory: null,
      selectedReceiptIds: [],
      quickFilter: "all",
    };
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return {
        search: "",
        selectedCategory: null,
        selectedReceiptIds: [],
        quickFilter: "all",
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
      quickFilter: parseQuickFilter(parsed.quickFilter),
    };
  } catch {
    return {
      search: "",
      selectedCategory: null,
      selectedReceiptIds: [],
      quickFilter: "all",
    };
  }
}

export const useFilterStore = defineStore("filters", () => {
  const initial = readStoredFilters();
  const search = ref(initial.search);
  const selectedCategory = ref<string | null>(initial.selectedCategory);
  const selectedReceiptIds = ref<string[]>(initial.selectedReceiptIds);
  const quickFilter = ref<ArticleQuickFilter>(initial.quickFilter);

  watch(
    [search, selectedCategory, selectedReceiptIds, quickFilter],
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
          quickFilter: quickFilter.value,
        } satisfies StoredFilters)
      );
    },
    { deep: true }
  );

  function resetFilters() {
    search.value = "";
    selectedCategory.value = null;
    selectedReceiptIds.value = [];
    quickFilter.value = "all";
  }

  return {
    search,
    selectedCategory,
    selectedReceiptIds,
    quickFilter,
    resetFilters,
  };
});
