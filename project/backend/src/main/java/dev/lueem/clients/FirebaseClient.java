package dev.lueem.clients;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.lueem.model.Article;

@ApplicationScoped
public class FirebaseClient {

    @ConfigProperty(name = "firebase.api.url")
    private String firebaseApiUrl;

    @ConfigProperty(name = "firebase.api.key")
    private String firebaseApiKey;

    private static final Logger LOGGER = Logger.getLogger(FirebaseClient.class.getName());
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sendet eine Liste von Artikeln an Firebase zur Persistierung.
     *
     * @param articles die Liste von Artikeln, die gespeichert werden sollen
     * @return die gespeicherten Artikel von Firebase
     */
    public List<Article> insertArticles(List<Article> articles) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(articles);
            LOGGER.info("Payload sent to Firebase: " + jsonPayload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(firebaseApiUrl + "/articles"))
                    .header("Content-Type", "application/json")
                    .header("apikey", firebaseApiKey)
                    .header("Authorization", "Bearer " + firebaseApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Response from Firebase: " + response.body());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                // Firebase gibt die eingefügten Datensätze zurück
                try (JsonReader jsonReader = Json.createReader(new StringReader(response.body()))) {
                    JsonArray jsonArray = jsonReader.readArray();
                    return objectMapper.readValue(jsonArray.toString(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, Article.class));
                }
            } else {
                LOGGER.severe("Failed to save articles to Firebase. Status: " + response.statusCode());
                throw new RuntimeException("Failed to save articles to Firebase. Status: " + response.statusCode());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving articles to Firebase", e);
            throw new RuntimeException("Error saving articles to Firebase", e);
        }
    }
}
