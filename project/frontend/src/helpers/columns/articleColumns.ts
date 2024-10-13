import { Column } from "../interfaces/column.interface";

export const articleColumns: Column[] = [
  {
    name: "Purchase_Date",
    label: "Kaufdatum",
    field: "Purchase_Date",
    sortable: true,
    align: "center",
  },
  {
    name: "Category",
    label: "Kategorie",
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
  { name: "Quantity", label: "Menge", field: "Quantity", sortable: true },
  {
    name: "Total",
    label: "Total (nach Rabatt)",
    field: "Total",
    sortable: true,
  },
];
