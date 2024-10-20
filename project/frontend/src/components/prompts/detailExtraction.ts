export const formatArticlesForDetailExtraction = (articles: any[]): string => {
  return articles
    .map(
      (article) =>
        `Id: ${article.id}, Name: ${article.name}, Quantity: ${article.quantity}`
    )
    .join("\n");
};
