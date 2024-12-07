export interface Recipe {
  id?: string;
  title: string;
  description: string;
  cookingTime: string;
  category: string;
  servings: number;
  color: string;
  ingredients: string[];
  stepsList: string[];
  image?: string;
}
