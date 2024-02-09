package dev.lueem.payload;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@ApplicationScoped
public class PayloadBuilder {

        @ConfigProperty(name = "OPENAI_MODEL", defaultValue = "gpt-3.5-turbo-1106")
        private String model = "gpt-3.5-turbo-1106";

        public String constructPayload(String question) {

                // Create the sample json payload
                JsonArrayBuilder articles = Json.createArrayBuilder();
                JsonObjectBuilder article = Json.createObjectBuilder();
                article.add("Name", "QP Früchtequark Erdbeer 2x125g");
                article.add("Price", 1.2);
                article.add("Quantity", 1);
                article.add("Discount", 0.0);

                articles.add(article);

                JsonObject articleModel = Json.createObjectBuilder()
                                .add("ArticleList", articles)
                                .build();

                JsonObjectBuilder messageSystem = Json.createObjectBuilder();
                messageSystem.add("role", "system");
                messageSystem.add("content", "Output JSON Object in this format: " + articleModel.toString());

                JsonObjectBuilder messageUser = Json.createObjectBuilder()
                                .add("role", "user")
                                .add("content", question);

                JsonArrayBuilder messages = Json.createArrayBuilder()
                                .add(messageUser)
                                .add(messageSystem);

                JsonObject payload = Json.createObjectBuilder()
                                .add("model", model)
                                .add("messages", messages)
                                .add("response_format", Json.createObjectBuilder().add("type", "json_object"))
                                .build();

                return payload.toString();
        }

        public String constructReceiptPayload(String question) {

                // Create the sample json payload
                JsonArrayBuilder receipes = Json.createArrayBuilder();
                JsonObjectBuilder receipe = Json.createObjectBuilder();
                receipe.add("Name", "Rezeptname");
                receipe.add("Ingredients", "Artikel 1, Artikel 2");
                receipe.add("Instructions", "kurze Anleitung");
                receipe.add("Servings", "2");
                receipe.add("PreparationTime", "minutes");

                receipes.add(receipe);

                JsonObject receiptModel = Json.createObjectBuilder()
                                .add("ReceiptList", receipes)
                                .build();

                JsonObjectBuilder messageSystem = Json.createObjectBuilder();
                messageSystem.add("role", "system");
                messageSystem.add("content", "Output JSON Object in this format: " + receiptModel.toString());
                JsonObjectBuilder messageUser = Json.createObjectBuilder()
                                .add("role", "user")
                                .add("content", question);

                JsonArrayBuilder messages = Json.createArrayBuilder()
                                .add(messageUser)
                                .add(messageSystem);

                JsonObject payload = Json.createObjectBuilder()
                                .add("model", model)
                                .add("messages", messages)
                                .add("response_format", Json.createObjectBuilder().add("type", "json_object"))
                                .build();
                return payload.toString();
        }

}
