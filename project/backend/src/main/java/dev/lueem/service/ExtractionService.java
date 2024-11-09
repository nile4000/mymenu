package dev.lueem.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import dev.lueem.util.FileUtils;
import dev.lueem.util.TextUtils;
import dev.lueem.clients.OpenAiClient;
import dev.lueem.model.Article;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonArrayBuilder;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ExtractionService {

    private final OpenAiClient openAiClient;
    private final TextUtils textUtils;

    private static final Logger LOGGER = Logger.getLogger(ExtractionService.class.getName());
    private static final String FILE_REASON_HEADER = "Reason";
    private static final String QUESTION_PREFIX = "Extract articles from the given receipt and return a list in a valid JSON format.\n"
            +
            "Each article should include the following fields: Name, Price, Quantity, Discount, Total (or 0 if none).\n";

    @Inject
    public ExtractionService(OpenAiClient openAiClient, TextUtils textUtils) {
        this.openAiClient = openAiClient;
        this.textUtils = textUtils;
    }

    public Response extractArticles(MultipartFormDataInput input) {
        try {
            File pdfFile = FileUtils.extractPdfFromMultipart(input);

            if (pdfFile == null) {
                LOGGER.warning("No PDF file found in the request.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(FILE_REASON_HEADER, "No PDF file found in the request")
                        .build();
            }

            // Extract text
            String documentContent = FileUtils.getTextFromFile(pdfFile);
            if (documentContent == null) {
                LOGGER.warning("Failed to extract text from the file.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .header(FILE_REASON_HEADER, "Failed to extract text from the file")
                        .build();
            }

            String cleanedContent = textUtils.cleanUpContent(documentContent);
            String extractTotal = textUtils.extractTotal(cleanedContent);
            String extractDate = textUtils.extractDate(cleanedContent);
            String cuttedEnd = textUtils.extractArticlesUntilTotal(cleanedContent);
            int totalRowNumber = textUtils.findTotalRowNumber(cuttedEnd);

            LOGGER.info("Extracted Total: " + extractTotal);
            LOGGER.info("Extracted Date: " + extractDate);
            LOGGER.info("Cutted End Content: " + cuttedEnd);

            // OpenAI request
            JsonArray articlesJson = getAnswerOpenAI(cuttedEnd);
            if (articlesJson == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to retrieve articles from OpenAI.")
                        .build();
            }

            LOGGER.info("Raw Articles JSON: " + articlesJson.toString());
            JsonArray sanitizedArticlesJson = sanitizeArticlesJson(articlesJson);
            LOGGER.info("Sanitized Articles JSON: " + sanitizedArticlesJson.toString());

            // Convert sanitized JSON to Article objects
            List<Article> articles = convertJsonArrayToArticles(sanitizedArticlesJson);
            LOGGER.info("Number of Articles Extracted: " + articles.size());

            String corp = textUtils.extractCorp(cleanedContent);
            UUID uid = UUID.randomUUID();

            JsonObjectBuilder jsonResponseBuilder = Json.createObjectBuilder();

            // Add UID
            jsonResponseBuilder.add("UID", uid.toString());

            // Add Purchase_Date
            if (extractDate != null) {
                jsonResponseBuilder.add("Purchase_Date", extractDate);
            } else {
                jsonResponseBuilder.add("Purchase_Date", "Unknown");
            }

            if (totalRowNumber >= 0) {
                jsonResponseBuilder.add("Total_R_Extract", totalRowNumber);
            } else {
                jsonResponseBuilder.add("Total_R_Extract", "0");
            }

            if (articles.size() != 0) {
                jsonResponseBuilder.add("Total_R_Open_Ai", articles.size());
            } else {
                jsonResponseBuilder.add("Total_R_Open_Ai", "0");
            }

            // Add Corp
            if (corp != null) {
                jsonResponseBuilder.add("Corp", corp);
            } else {
                jsonResponseBuilder.add("Corp", "Unknown");
            }

            // Add Total
            if (extractTotal != null) {
                jsonResponseBuilder.add("Total", extractTotal);
            } else {
                jsonResponseBuilder.add("Total", "0.0");
            }

            // Add Articles
            jsonResponseBuilder.add("Articles", sanitizedArticlesJson);

            JsonObject jsonResponse = jsonResponseBuilder.build();

            LOGGER.info("JSON Response Built Successfully.");

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
        try {
            JsonArray response = openAiClient.askQuestion(fullQuestion, "article");
            if (response == null || response.isEmpty()) {
                LOGGER.severe("OpenAI response is null or empty.");
                return Json.createArrayBuilder().build();
            }
            return response;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while communicating with OpenAI", e);
            return Json.createArrayBuilder().build();
        }
    }

    private JsonArray sanitizeArticlesJson(JsonArray articlesJson) {
        JsonArrayBuilder sanitizedArrayBuilder = Json.createArrayBuilder();

        for (jakarta.json.JsonValue jsonValue : articlesJson) {
            if (jsonValue.getValueType() != jakarta.json.JsonValue.ValueType.OBJECT) {
                LOGGER.warning("Non-object JSON value found in articles array: " + jsonValue);
                continue; // Skip invalid entries
            }

            jakarta.json.JsonObject jsonObject = jsonValue.asJsonObject();
            JsonObjectBuilder sanitizedObjectBuilder = Json.createObjectBuilder();

            // Define expected fields and their default values
            Map<String, Object> fieldsWithDefaults = new HashMap<>();
            fieldsWithDefaults.put("Name", "");
            fieldsWithDefaults.put("Price", 0.0);
            fieldsWithDefaults.put("Quantity", 0.0);
            fieldsWithDefaults.put("Discount", 0.0);
            fieldsWithDefaults.put("Total", 0.0);

            for (Map.Entry<String, Object> entry : fieldsWithDefaults.entrySet()) {
                String field = entry.getKey();
                Object defaultValue = entry.getValue(); // Corrected

                if (jsonObject.containsKey(field) && !jsonObject.isNull(field)) {
                    try {
                        // Add the field as is, assuming correct type
                        sanitizedObjectBuilder.add(field, jsonObject.get(field));
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING,
                                "Invalid value for field '" + field + "' in article: " + jsonObject.toString(), e);
                        // Assign default value on error
                        if (defaultValue instanceof String) {
                            sanitizedObjectBuilder.add(field, (String) defaultValue);
                        } else if (defaultValue instanceof Double) {
                            sanitizedObjectBuilder.add(field, (Double) defaultValue);
                        } else {
                            sanitizedObjectBuilder.addNull(field);
                        }
                    }
                } else {
                    // Assign default value based on field type
                    if (defaultValue instanceof String) {
                        sanitizedObjectBuilder.add(field, (String) defaultValue);
                    } else if (defaultValue instanceof Double) {
                        sanitizedObjectBuilder.add(field, (Double) defaultValue);
                    } else {
                        sanitizedObjectBuilder.addNull(field);
                    }
                }
            }

            sanitizedArrayBuilder.add(sanitizedObjectBuilder);
        }

        return sanitizedArrayBuilder.build();
    }

    private List<Article> convertJsonArrayToArticles(JsonArray articlesJson) {
        List<Article> articles = new ArrayList<>();
        for (jakarta.json.JsonValue jsonValue : articlesJson) {
            if (jsonValue.getValueType() != jakarta.json.JsonValue.ValueType.OBJECT) {
                LOGGER.warning(
                        "Invalid JSON value type for article. Expected OBJECT, found: " + jsonValue.getValueType());
                continue; // Skip invalid entries
            }

            jakarta.json.JsonObject jsonObject = jsonValue.asJsonObject();
            Article article = new Article();

            try {
                // Handle 'Name' field
                String name = jsonObject.containsKey("Name") && !jsonObject.isNull("Name")
                        ? jsonObject.getString("Name")
                        : "Unknown";
                article.setName(name);

                // Handle 'Price' field
                BigDecimal price = jsonObject.containsKey("Price") && !jsonObject.isNull("Price")
                        ? jsonObject.getJsonNumber("Price").bigDecimalValue()
                        : BigDecimal.ZERO;
                article.setPrice(price);

                // Handle 'Quantity' field
                BigDecimal quantity = jsonObject.containsKey("Quantity") && !jsonObject.isNull("Quantity")
                        ? jsonObject.getJsonNumber("Quantity").bigDecimalValue()
                        : BigDecimal.ZERO;
                article.setQuantity(quantity);

                // Handle 'Discount' field
                BigDecimal discount = jsonObject.containsKey("Discount") && !jsonObject.isNull("Discount")
                        ? jsonObject.getJsonNumber("Discount").bigDecimalValue()
                        : BigDecimal.ZERO;
                article.setDiscount(discount);

                // Handle 'Total' field
                BigDecimal total = jsonObject.containsKey("Total") && !jsonObject.isNull("Total")
                        ? jsonObject.getJsonNumber("Total").bigDecimalValue()
                        : BigDecimal.ZERO;
                article.setTotal(total);

                // Initialize empty 'Category' field, its filled later
                article.setCategory("");

                // Correct data inconsistencies
                correctArticleData(article);

                articles.add(article);
            } catch (NumberFormatException | ClassCastException e) {
                LOGGER.log(Level.SEVERE, "Error parsing article fields: " + jsonObject.toString(), e);
                // Optionally, skip adding this article or add it with default values
            }
        }
        return articles;
    }

    private void correctArticleData(Article article) {
        // Correct Quantity if negative
        if (article.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            article.setQuantity(BigDecimal.ZERO);
        }

        // Correct Total
        BigDecimal calculatedTotal = article.getPrice()
                .multiply(article.getQuantity())
                .subtract(article.getDiscount());
        article.setTotal(calculatedTotal.max(BigDecimal.ZERO)); 
    }

}
