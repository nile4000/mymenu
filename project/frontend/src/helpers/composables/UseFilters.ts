import { ref, computed } from "vue";
import { Article } from "../interfaces/article.interface";

export function useFilters(rows: Article[]) {
  const search = ref("");
  const selectedCategory = ref<string | null>(null);
  const selectedReceiptIds = ref<string[]>([]);
  const filterFields = ["Name", "Purchase_Date", "Category"] as const;

  const filteredRows = computed(() => {
    let filtered = rows;
    const cleanedSearch = search.value.trim().toLowerCase();
    if (cleanedSearch) {
      filtered = filtered.filter((row) =>
        filterFields.some((field) => {
          const fieldValue = row[field];
          return fieldValue && String(fieldValue).toLowerCase().includes(cleanedSearch);
        })
      );
    }

    if (selectedReceiptIds.value.length > 0) {
      filtered = filtered.filter(
        (row) => row.Receipt_Id && selectedReceiptIds.value.includes(String(row.Receipt_Id))
      );
    } else {
      filtered = [];
    }

    if (selectedCategory.value) {
      filtered = filtered.filter((row) => row.Category === selectedCategory.value);
    }

    return filtered;
  });

  return {
    search,
    selectedCategory,
    selectedReceiptIds,
    filteredRows,
  };
}
