package dev.lueem.ai;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.StringReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.lueem.payload.PayloadBuilder;

@ApplicationScoped
public class OpenAiClient {

    @ConfigProperty(name = "OPENAI_API_KEY")
    private String openaiKey;

    private static final Logger LOGGER = Logger.getLogger(OpenAiClient.class.getName());
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Inject
    private PayloadBuilder payloadBuilder;

    private JsonArray processResponse(String responseBody, String type) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
            JsonObject jsonResponse = jsonReader.readObject();
            JsonArray choices = jsonResponse.getJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices found in the response.");
            }
            JsonObject message = choices.getJsonObject(0).getJsonObject("message");
            if (message == null) {
                throw new RuntimeException("No message found in the first choice.");
            }
            String contentString = message.getString("content");

            LOGGER.info("Content received from OpenAI: " + contentString);

            try (JsonReader contentReader = Json.createReader(new StringReader(contentString))) {
                JsonObject contentJson = contentReader.readObject();
                return contentJson.getJsonArray(type.equals("receipt") ? "ReceiptList" : "ArticleList");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing response", e);
            throw new RuntimeException("Error processing response", e);
        }
    }

    public JsonArray askQuestion(String question, String type) {
        try {
            String payload = payloadBuilder.constructPayload(question, type);
            LOGGER.info("Payload sent to OpenAI: " + payload);

            if (openaiKey == null || openaiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API KEY is not set");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openaiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Response from OpenAI: " + response.body());

            return processResponse(response.body(), type);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in askQuestion", e);
            return Json.createArrayBuilder().build(); // Return an empty JsonArray on error
        }
    }
}
