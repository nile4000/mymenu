import { defineStore } from "pinia";
import { Category, listCategories } from "src/services";

interface CategoryState {
  categories: Category[];
  loaded: boolean;
  loading: Promise<void> | null;
}

export const useCategoryStore = defineStore("categories", {
  state: (): CategoryState => ({
    categories: [],
    loaded: false,
    loading: null,
  }),
  getters: {
    names(state): string[] {
      return state.categories.map((c) => c.name);
    },
    metaByName(state): Record<string, { icon: string; color: string }> {
      return state.categories.reduce<Record<string, { icon: string; color: string }>>(
        (acc, { name, icon, color }) => {
          acc[name] = { icon, color };
          return acc;
        },
        {}
      );
    },
  },
  actions: {
    async ensureLoaded(): Promise<void> {
      if (this.loaded) return;
      if (this.loading) return this.loading;

      this.loading = (async () => {
        const result = await listCategories();
        if (result.ok) {
          this.categories = result.data;
          this.loaded = true;
        }
      })();

      try {
        await this.loading;
      } finally {
        this.loading = null;
      }
    },
  },
});
