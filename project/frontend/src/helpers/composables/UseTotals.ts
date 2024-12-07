import { computed } from "vue";
import { Article } from "../interfaces/article.interface";
import { Receipt } from "../interfaces/receipt.interface";
import { TotalExpenses } from "../interfaces/totalExpenses.interface";

export function useTotals(rows: Article[], receipts: Record<string, Receipt>) {
  const totalsPerReceipt = computed(() => {
    return Object.values(receipts).map((receipt) => ({
      id: receipt.Id || "",
      date: receipt.Purchase_Date,
      total: parseFloat(receipt.Total_Receipt.toString()),
    }));
  });

  const totalExpenses = computed<TotalExpenses>(() => {
    const receiptValues = Object.values(receipts);
    const sum = receiptValues.reduce((sum, receipt) => {
      return sum + parseFloat(receipt.Total_Receipt.toString());
    }, 0);

    if (receiptValues.length === 0) {
      return {
        sum,
        firstMonth: null,
        firstYear: null,
        lastMonth: null,
        lastYear: null,
      };
    }

    // Sort receipts by Purchase_Date
    const sortedReceipts = receiptValues
      .map((receipt) => new Date(receipt.Purchase_Date))
      .sort((a, b) => a.getTime() - b.getTime());

    const firstDate = sortedReceipts[0];
    const lastDate = sortedReceipts[sortedReceipts.length - 1];

    return {
      sum,
      firstMonth: firstDate.getMonth() + 1, // getMonth() ist nullbasiert
      firstYear: firstDate.getFullYear(),
      lastMonth: lastDate.getMonth() + 1,
      lastYear: lastDate.getFullYear(),
    };
  });

  const totalsPerCategory = computed(() => {
    return rows.reduce((acc, article) => {
      const category = article.Category;
      if (category) {
        if (!acc[category]) {
          acc[category] = 0;
        }
        acc[category] += parseFloat(article.Total.toString());
      }
      return acc;
    }, {} as Record<string, number>);
  });

  const calculatedTotalPerReceipt = computed(() => {
    return rows.reduce((acc, article) => {
      const receiptId = article.Receipt_Id;
      if (receiptId) {
        if (!acc[receiptId]) {
          const receipt = receipts[receiptId];
          acc[receiptId] = {
            total: 0,
            date: receipt ? receipt.Purchase_Date : "Unbekannt",
          };
        }
        acc[receiptId].total += article.Total;
      }
      return acc;
    }, {} as Record<string, { total: number; date: string }>);
  });

  return {
    totalsPerCategory,
    totalsPerReceipt,
    totalExpenses,
    calculatedTotalPerReceipt,
  };
}
