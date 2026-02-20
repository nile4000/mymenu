import { storeToRefs } from "pinia";
import { computed, Ref, unref } from "vue";
import { Article } from "../interfaces/article.interface";
import { useFilterStore } from "src/stores/filterStore";

export function useFilters(rows: Article[] | Ref<Article[]>) {
  const filterStore = useFilterStore();
  const { search, selectedCategory, selectedReceiptIds } = storeToRefs(filterStore);
  const filterFields = ["Name", "Purchase_Date", "Category"] as const;

  const filteredRows = computed(() => {
    const cleanedSearch = search.value.trim().toLowerCase();
    const normalizedSelectedReceiptIds = (selectedReceiptIds.value || []).map((id) =>
      String(id)
    );

    return unref(rows).filter((row) => {
      const matchesSearch =
        !cleanedSearch ||
        filterFields.some((field) =>
          row[field]
            ? String(row[field]).toLowerCase().includes(cleanedSearch)
            : false
        );

      const matchesReceipt =
        normalizedSelectedReceiptIds.length === 0 ||
        (row.Receipt_Id &&
          normalizedSelectedReceiptIds.includes(String(row.Receipt_Id)));

      const matchesCategory =
        !selectedCategory.value || row.Category === selectedCategory.value;

      return matchesSearch && matchesReceipt && matchesCategory;
    });
  });

  return {
    search,
    selectedCategory,
    selectedReceiptIds,
    filteredRows,
  };
}
