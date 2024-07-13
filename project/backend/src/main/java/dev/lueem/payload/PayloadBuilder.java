package dev.lueem.payload;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@ApplicationScoped
public class PayloadBuilder {

        @ConfigProperty(name = "OPENAI_MODEL", defaultValue = "gpt-4o")
        private String modelDefault = "gpt-4o";
        // private String modelReceipt = "gpt-4-0125-preview";

        private JsonObject getArticleModel() {
                JsonObjectBuilder article = Json.createObjectBuilder()
                                .add("Name", "QP Früchtequark Erdbeer 2x125g")
                                .add("Price", 1.2)
                                .add("Quantity", 1)
                                .add("Discount", 0.0);

                return Json.createObjectBuilder()
                                .add("ArticleList", Json.createArrayBuilder()
                                                .add(article))
                                .build();
        }

        private JsonObject getReceiptModel() {
                JsonObjectBuilder receipt = Json.createObjectBuilder()
                                .add("Name", "Rezeptname")
                                .add("Ingredients", "Artikel 1, Artikel 2")
                                .add("Instructions", "Anleitung")
                                .add("Servings", "Personen")
                                .add("PreparationTime", "Minuten");

                return Json.createObjectBuilder()
                                .add("ReceiptList", Json.createArrayBuilder()
                                                .add(receipt))
                                .build();
        }

        public String constructPayload(String question) {

                // Create the sample json payload
                JsonObject articleModel = getArticleModel();

                JsonObjectBuilder messageSystem = Json.createObjectBuilder();
                messageSystem.add("role", "system");
                messageSystem.add("content", "Output JSON Object in this format: " + articleModel.toString());

                JsonObjectBuilder messageUser = Json.createObjectBuilder()
                                .add("role", "user")
                                .add("content", question);

                return constructGPTMessages(messageSystem, messageUser);
        }

        public String constructReceiptPayload(String question) {

                // Create the sample json payload
                JsonObject receiptModel = getReceiptModel();

                JsonObjectBuilder messageSystem = Json.createObjectBuilder();
                messageSystem.add("role", "system");
                messageSystem.add("content", "Output JSON Object in this format: " + receiptModel.toString());

                JsonObjectBuilder messageUser = Json.createObjectBuilder()
                                .add("role", "user")
                                .add("content", question);

                return constructGPTMessages(messageSystem, messageUser);
        }

        private String constructGPTMessages(JsonObjectBuilder messageSystem, JsonObjectBuilder messageUser) {
                JsonArrayBuilder messages = Json.createArrayBuilder()
                                .add(messageUser)
                                .add(messageSystem);

                JsonObject payload = Json.createObjectBuilder()
                                .add("model", modelDefault)
                                .add("messages", messages)
                                .add("response_format", Json.createObjectBuilder().add("type", "json_object"))
                                .build();
                return payload.toString();
        }

}
