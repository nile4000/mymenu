import { ref, computed } from "vue";
import { Article } from "../interfaces/article.interface";

export function useFilters(rows: Article[]) {
  const search = ref("");
  const selectedCategory = ref<string | null>(null);
  const selectedReceiptIds = ref<string[]>([]);
  const filterFields = ["Name", "Purchase_Date", "Category"] as const;

  const filteredRows = computed(() => {
    const cleanedSearch = search.value.trim().toLowerCase();

    return rows.filter((row) => {
      const matchesSearch =
        !cleanedSearch ||
        filterFields.some((field) =>
          row[field]
            ? String(row[field]).toLowerCase().includes(cleanedSearch)
            : false
        );

      const matchesReceipt =
        selectedReceiptIds.value.length === 0 ||
        (row.Receipt_Id && selectedReceiptIds.value.includes(String(row.Receipt_Id)));

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
