export const formatArticlesForDetailExtraction = (articles: any[]): string => {
  return articles
    .map((article) => {
      if (article.quantity < 1) {
        return `Id: ${article.id}, Name: ${article.name} x ${article.quantity}kg`;
      } else if (article.quantity === 1) {
        // Handling for quantity equal to 1
        return `Id: ${article.id}, Name: ${article.name}, Quantity: ${article.quantity}`;
      } else {
        // Handling for quantities greater than 1
        return `Id: ${article.id}, Name: ${article.name} x ${article.quantity}`;
      }
    })
    .join("\n");
};

