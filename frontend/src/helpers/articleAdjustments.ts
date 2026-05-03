import { Article } from "./interfaces/article.interface";

type ArticleAdjustmentFields = Pick<Article, "Name"> & { Total?: number | null };

export function isArticleAdjustment(article: ArticleAdjustmentFields): boolean {
  return (article.Total ?? 0) < 0 || article.Name.trim().toLowerCase().startsWith("rabatt");
}

export function withoutArticleAdjustments<T extends ArticleAdjustmentFields>(articles: T[]): T[] {
  return articles.filter((article) => !isArticleAdjustment(article));
}
