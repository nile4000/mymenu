export interface RecipeIngredient {
  name: string;
  amount: number;
  unit: string;
}

export interface Recipe {
  id?: string;
  title: string;
  description: string;
  cookingTime: string;
  category: string;
  servings: number;
  color: string;
  ingredients: RecipeIngredient[];
  stepsList: string[];
  image?: string;
}
