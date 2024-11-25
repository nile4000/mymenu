import { formatDate } from "../dateHelpers";
import { Column } from "../interfaces/column.interface";

export const articleColumns: Column[] = [
  // {
  //   name: "Name",
  //   required: true,
  //   label: "Name",
  //   align: "left",
  //   field: "Name",
  //   sortable: true,
  // },
  // {
  //   name: "Category",
  //   label: "Kategorie",
  //   field: "Category",
  //   sortable: true,
  //   align: "center",
  //   icon: "edit",
  // },
  {
    name: "Purchase_Date",
    label: "Kaufdatum",
    field: "Purchase_Date",
    format: (val: string) => formatDate(val),
    sortable: true,
    style: 'width: 100px',
    align: "left",
  },
  // {
  //   name: "Price",
  //   align: "center",
  //   label: "Preis",
  //   field: "Price",
  //   sortable: true,
  // },
  // { name: "Discount", label: "Rabatt", field: "Discount", sortable: true },
  {
    name: "Quantity",
    label: "Menge",
    field: "Quantity",
    align: "center",
    sortable: true,
  },
  { name: "Unit", label: "Einheit", field: "Unit", sortable: true },
  // {
  //   name: "Base_Unit",
  //   label: "Einheit pro 100g/100ml/Stk",
  //   field: "Base_Unit",
  //   sortable: true,
  // },
  // {
  //   name: "Price_Base_Unit",
  //   label: "Preis pro 100g/100ml/Stk",
  //   field: "Price_Base_Unit",
  //   format: (val: number) => {
  //     if (typeof val === "number") {
  //       return val.toFixed(3);
  //     } else {
  //       if (isNaN(val)) {
  //         return parseFloat(val).toFixed(3);
  //       }
  //     }
  //   },
  //   sortable: true,
  // },
  {
    name: "Total",
    label: "Total CHF",
    field: "Total",
    sortable: true,
  },
];

export const articleColumnsList: Column[] = [
  {
    name: "Name",
    required: true,
    label: "Name",
    align: "left",
    field: "Name",
    sortable: true,
  },
  {
    name: "Category",
    label: "Kategorie",
    field: "Category",
    sortable: true,
    align: "center",
    icon: "edit",
  },
  {
    name: "Purchase_Date",
    label: "Kaufdatum",
    field: "Purchase_Date",
    format: (val: string) => formatDate(val),
    sortable: true,
    style: 'width: 100px',
    align: "left",
  },
  // {
  //   name: "Price",
  //   align: "center",
  //   label: "Preis",
  //   field: "Price",
  //   sortable: true,
  // },
  // { name: "Discount", label: "Rabatt", field: "Discount", sortable: true },
  {
    name: "Quantity",
    label: "Menge",
    field: "Quantity",
    align: "center",
    sortable: true,
  },
  { name: "Unit", label: "Einheit", field: "Unit", sortable: true },
  // {
  //   name: "Base_Unit",
  //   label: "Einheit pro 100g/100ml/Stk",
  //   field: "Base_Unit",
  //   sortable: true,
  // },
  // {
  //   name: "Price_Base_Unit",
  //   label: "Preis pro 100g/100ml/Stk",
  //   field: "Price_Base_Unit",
  //   format: (val: number) => {
  //     if (typeof val === "number") {
  //       return val.toFixed(3);
  //     } else {
  //       if (isNaN(val)) {
  //         return parseFloat(val).toFixed(3);
  //       }
  //     }
  //   },
  //   sortable: true,
  // },
  {
    name: "Total",
    label: "Total CHF",
    field: "Total",
    sortable: true,
  },
];
