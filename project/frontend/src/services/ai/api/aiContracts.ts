import { Recipe } from "src/helpers/interfaces/recipe.interface";

export type CategorizeItem = {
  id: string;
  name: string;
};

export type ExtractUnitItem = {
  id: string;
  name: string;
  quantity: number;
  price: number;
};

export type RecipeItem = {
  name: string;
  quantity: number;
  unit: string;
};

export type CategorizeResultItem = {
  id: string;
  category: string;
};

export type ExtractUnitResultItem = {
  id: string;
  unit: string;
};

export type RecipeResponse = Recipe;
