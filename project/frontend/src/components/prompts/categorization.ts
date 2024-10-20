export const formatArticlesForCategorization = (articles: any[]): string => {
  return articles.map(article => `ID: ${article.id}, Name: ${article.name}`).join('\n');
};

export const categories: string[] = [
  "Obst und Gemüse",
  "Milchprodukte, Eier und Alternativen",
  "Fleisch, Fisch und pflanzliche Proteine",
  "Backwaren und Getreide",
  "Softgetraenke",
  "Alkoholische/Alkoholfreie Getraenke",
  "Snacks, Apero und Suesswaren",
  "Reinigungsmittel und Haushaltsreiniger",
  "Koerperpflegeprodukte und Hygieneartikel",
  "Tierbedarf und Sonstiges"
];
