import { formatDate } from "../dateHelpers";
import { Column } from "../interfaces/column.interface";

export const articleColumns: Column[] = [
  {
    name: "Purchase_Date",
    label: "Kaufdatum",
    field: "Purchase_Date",
    format: (val: string) => formatDate(val),
    sortable: true,
    align: "center",
  },
  {
    name: "Category",
    label: "Kategorie (Extrahiert)",
    field: "Category",
    sortable: true,
    align: "center",
  },
  {
    name: "Name",
    required: true,
    label: "Name",
    align: "left",
    field: "Name",
    sortable: true,
  },
  {
    name: "Price",
    align: "center",
    label: "Preis",
    field: "Price",
    sortable: true,
  },
  { name: "Discount", label: "Rabatt", field: "Discount", sortable: true },
  { name: "Quantity", label: "Menge", field: "Quantity", sortable: true },
  { name: "Unit", label: "Einheit (Extrahiert)", field: "Unit", sortable: true },
  {
    name: "Total",
    label: "Total (nach Rabatt)",
    field: "Total",
    sortable: true,
  },
];
