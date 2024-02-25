package dev.lueem.payload;

import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@ApplicationScoped
public class PayloadBuilder {

        @ConfigProperty(name = "OPENAI_MODEL", defaultValue = "gpt-3.5-turbo-1106")
        private String modelDefault = "gpt-3.5-turbo-1106";
        // private String modelDefault = "gpt-4-0125-preview";

        private Map<String, String> articleData = Map.of(
                        "Name", "QP Früchtequark Erdbeer 2x125g",
                        "Price", "1.2",
                        "Quantity", "1",
                        "Discount", "0.0");

        private Map<String, String> receiptData = Map.of(
                        "Name", "Rezeptname",
                        "Ingredients", "Artikel 1, Artikel 2",
                        "Instructions", "kurze Anleitung",
                        "Servings", "2",
                        "PreparationTime", "minutes");

        private String getArticleModel() {
                return createSampleModel(articleData).toString();
        }

        private String getReceiptModel() {
                return createSampleModel(receiptData).toString();
        }

        private JsonObject createSampleModel(Map<String, String> data) {
                JsonArrayBuilder elements = Json.createArrayBuilder();
                JsonObjectBuilder element = Json.createObjectBuilder();
                for (Map.Entry<String, String> entry : data.entrySet()) {
                        element.add(entry.getKey(), entry.getValue());
                }
                elements.add(element);

                return Json.createObjectBuilder().add("List", elements).build();
        }

        public String constructPayload(String question) {

                // Create the sample json payload
                String articleModel = getArticleModel();
                JsonObjectBuilder messageSystem = Json.createObjectBuilder();
                messageSystem.add("role", "system");
                messageSystem.add("content", "Output JSON Object in this format: " + articleModel);
                JsonObjectBuilder messageUser = Json.createObjectBuilder()
                                .add("role", "user")
                                .add("content", question);
                return constructGPTMessages(messageSystem, messageUser);
        }

        public String constructReceiptPayload(String question) {

                // Create the sample json payload
                String receiptModel = getReceiptModel();
                JsonObjectBuilder messageSystem = Json.createObjectBuilder();
                messageSystem.add("role", "system");
                messageSystem.add("content", "Output JSON Object in this format: " + receiptModel);
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
