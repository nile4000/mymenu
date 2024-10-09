package dev.lueem.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import dev.lueem.util.FileUtils;
import dev.lueem.util.TextUtils;
import dev.lueem.clients.OpenAiClient;
import dev.lueem.model.Article;
import dev.lueem.repository.ArticleRepository;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ExtractionService {

    private final OpenAiClient openAiClient;
    private final TextUtils textUtils;
    private final ArticleRepository articleRepository;

    private static final Logger LOGGER = Logger.getLogger(ExtractionService.class.getName());
    private static final String FILE_REASON_HEADER = "Reason";
    private static final String QUESTION_PREFIX = "Extract articles from the given receipt and return a list in a valid JSON format. Each article should include the following fields: Name, Price, Quantity, Discount, Total (or 0 if none), and Category. Use the following categories for classification:\n"
            +
            "1. Obst und Gemüse\n" +
            "2. Milchprodukte und Alternativen\n" +
            "3. Fleisch, Fisch und pflanzliche Proteine\n" +
            "4. Getreide und Backwaren\n" +
            "5. Getränke\n" +
            "If an article does not fit into any of these categories, assign it to the category 'Andere'.\n\n";

    @Inject
    public ExtractionService(OpenAiClient openAiClient, TextUtils textUtils, ArticleRepository articleRepository) {
        this.openAiClient = openAiClient;
        this.textUtils = textUtils;
        this.articleRepository = articleRepository;
    }

    public Response extractArticles(MultipartFormDataInput input) {
        try {
            File pdfFile = FileUtils.extractPdfFromMultipart(input);

            if (pdfFile == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(FILE_REASON_HEADER, "No PDF file found in the request")
                        .build();
            }

            // Extract text
            String documentContent = FileUtils.getTextFromFile(pdfFile);
            if (documentContent == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(FILE_REASON_HEADER, "Failed to extract text from the file")
                        .build();
            }

            String cleanedContent = textUtils.cleanUpContent(documentContent);
            String extractTotal = textUtils.extractTotal(cleanedContent);
            String extractDate = textUtils.extractDate(cleanedContent);
            String cuttedEnd = textUtils.extractArticlesUntilTotal(cleanedContent);

            // OpenAI request
            JsonArray articlesJson = getAnswerOpenAI(cuttedEnd);

            // Convert and Save - for future use
            // List<Article> articles = convertJsonArrayToArticles(articlesJson);
            // articleRepository.saveAll(articles);

            String corp = textUtils.extractCorp(cleanedContent);
            UUID uid = UUID.randomUUID();
            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("UID", uid.toString())
                    .add("PurchaseDate", extractDate)
                    .add("Corp", corp)
                    .add("Total", extractTotal)
                    .add("Articles", articlesJson)
                    .build();

            return Response.ok(jsonResponse)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in extracting Articles from PDF", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private JsonArray getAnswerOpenAI(String extractedText) {
        String fullQuestion = QUESTION_PREFIX + extractedText;
        return openAiClient.askQuestion(fullQuestion, "article");
    }

    private List<Article> convertJsonArrayToArticles(JsonArray articlesJson) {
        List<Article> articles = new java.util.ArrayList<>();
        for (jakarta.json.JsonValue jsonValue : articlesJson) {
            jakarta.json.JsonObject jsonObject = jsonValue.asJsonObject();
            Article article = new Article();
            article.setName(jsonObject.getString("Name"));
            article.setPrice(new BigDecimal(jsonObject.getString("Price")));
            article.setQuantity(jsonObject.getInt("Quantity"));
            article.setDiscount(new BigDecimal(jsonObject.getString("Discount")));
            article.setTotal(new BigDecimal(jsonObject.getString("Total")));
            article.setCategory(jsonObject.getString("Category"));
            articles.add(article);
        }
        return articles;
    }
}
