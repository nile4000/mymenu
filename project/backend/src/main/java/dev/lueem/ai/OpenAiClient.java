package dev.lueem.ai;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.StringReader;

import dev.lueem.payload.*;

@ApplicationScoped
public class OpenAiClient {

    @Inject
    private PayloadBuilder payloadBuilder;

    private static final Logger LOGGER = Logger.getLogger(OpenAiClient.class.getName());

    @ConfigProperty(name = "OPENAI_API_KEY")
    private String openaiKey = "";

    private final HttpClient httpClient;

    public OpenAiClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    private JsonArray processResponse(String responseBody) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
            JsonObject jsonResponse = jsonReader.readObject();
            String contentString = jsonResponse.getJsonArray("choices")
                    .getJsonObject(0)
                    .getJsonObject("message")
                    .getString("content");

            try (JsonReader contentReader = Json.createReader(new StringReader(contentString))) {
                JsonObject contentJson = contentReader.readObject();

                JsonArray articlesArray = contentJson.getJsonArray("ArticleList");
                return articlesArray;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing response", e);
            throw new RuntimeException("Error processing response", e);
        }
    }

    public JsonArray askQuestion(String question) {
        try {
            String payload = payloadBuilder.constructPayload(question);

            String key = ConfigProvider.getConfig().getValue("OPENAI_API_KEY", String.class);

            if (key == null || key.trim().isEmpty()) {
                throw new IllegalArgumentException("API KEY is not set");
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + key)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return processResponse(response.body());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in askQuestion", e);
            return Json.createArrayBuilder().build(); // Return an empty JsonArray on error
        }
    }
}
