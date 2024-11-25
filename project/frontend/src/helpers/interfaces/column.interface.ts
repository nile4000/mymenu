export interface Column {
  name: string;
  required?: boolean;
  label: string;
  align?: string;
  field: string;
  format?: any;
  style?: string;
  icon?: string;
  sortable: boolean;
}
