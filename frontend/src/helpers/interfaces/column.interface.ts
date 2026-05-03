export interface Column {
  name: string;
  required?: boolean;
  label: string;
  align?: 'left' | 'right' | 'center';
  field: string | ((row: any) => any);
  format?: any;
  style?: string;
  icon?: string;
  sortable: boolean;
}
