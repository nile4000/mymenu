import { storeToRefs } from "pinia";
import { computed, Ref, unref } from "vue";
import { Article } from "../interfaces/article.interface";
import { useFilterStore } from "src/stores/filterStore";
import { formatDate } from "../dateHelpers";

export function useFilters(rows: Article[] | Ref<Article[]>) {
  const filterStore = useFilterStore();
  const { search, selectedCategory, selectedReceiptIds } = storeToRefs(filterStore);
  const filterFields = ["Name", "Purchase_Date", "Category"] as const;

  const filteredRows = computed(() => {
    const sourceRows = unref(rows) ?? [];
    const cleanedSearch = search.value.trim().toLowerCase();
    const normalizedSelectedReceiptIds = (selectedReceiptIds.value || []).map((id) =>
      String(id)
    );

    return sourceRows.filter((row) => {
      const matchesSearch =
        !cleanedSearch ||
        filterFields.some((field) => {
          if (!row[field]) return false;
          const rawValue = String(row[field]).toLowerCase();
          if (rawValue.includes(cleanedSearch)) return true;
          if (field === "Purchase_Date") {
            return formatDate(row[field] as string).toLowerCase().includes(cleanedSearch);
          }
          return false;
        });

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
