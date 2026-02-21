import { computed, Ref, unref } from "vue";
import { Article } from "../interfaces/article.interface";
import { Receipt } from "../interfaces/receipt.interface";
import { TotalExpenses } from "../interfaces/totalExpenses.interface";

export function useTotals(
  rows: Article[] | Ref<Article[]>,
  receipts: Record<string, Receipt> | Ref<Record<string, Receipt>>
) {
  const safeReceipts = () => unref(receipts) ?? {};
  const safeRows = () => unref(rows) ?? [];

  const totalsPerReceipt = computed(() =>
    Object.values(safeReceipts()).map(({ Id, Purchase_Date, Total_Receipt }) => ({
      id: Id || "",
      date: Purchase_Date,
      total: Number(Total_Receipt),
    }))
  );

  const totalExpenses = computed<TotalExpenses>(() => {
    const receiptValues = Object.values(safeReceipts());
    const sum = receiptValues.reduce((acc, { Total_Receipt }) => acc + Number(Total_Receipt), 0);

    if (receiptValues.length === 0) {
      return {
        sum,
        firstMonth: null,
        firstYear: null,
        lastMonth: null,
        lastYear: null,
      };
    }

    const dates = receiptValues
      .map(({ Purchase_Date }) => new Date(Purchase_Date))
      .sort((a, b) => a.getTime() - b.getTime());

    const [firstDate, lastDate] = [dates[0], dates[dates.length - 1]];

    return {
      sum,
      firstMonth: firstDate.getMonth() + 1,
      firstYear: firstDate.getFullYear(),
      lastMonth: lastDate.getMonth() + 1,
      lastYear: lastDate.getFullYear(),
    };
  });

  const totalsPerCategory = computed(() =>
    safeRows().reduce((acc, { Category, Total }) => {
      if (Category) {
        acc[Category] = (acc[Category] ?? 0) + (Total ?? 0);
      }
      return acc;
    }, {} as Record<string, number>)
  );

  return {
    totalsPerCategory,
    totalsPerReceipt,
    totalExpenses,
  };
}
