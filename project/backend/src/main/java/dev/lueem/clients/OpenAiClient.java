package dev.lueem.clients;

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

    private JsonArray processResponse(String responseBody) {
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

            LOGGER.fine("Content received from OpenAI: " + contentString);

            try (JsonReader contentReader = Json.createReader(new StringReader(contentString))) {
                JsonObject contentJson = contentReader.readObject();
                return contentJson.getJsonArray("ArticleList");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing response", e);
            throw new RuntimeException("Error processing response", e);
        }
    }

    public JsonArray askQuestion(String question) {
        try {
            String payload = payloadBuilder.constructPayload(question);

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

            return processResponse(response.body());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in askQuestion", e);
            return Json.createArrayBuilder().build(); // Return an empty JsonArray on error, to complete the flow
        }
    }
}
