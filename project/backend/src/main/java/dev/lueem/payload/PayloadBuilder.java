package dev.lueem.payload;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

@ApplicationScoped
public class PayloadBuilder {

        @ConfigProperty(name = "OPENAI_MODEL", defaultValue = "gpt-3.5-turbo-0125")
        private String modelDefault = "gpt-3.5-turbo-0125";

        @ConfigProperty(name = "OPENAI_TEMPERATURE", defaultValue = "0.0")
        private double temperature;

        private JsonObject getModel() {
                return Json.createObjectBuilder()
                                .add("Name", "QP Früchtequark Erdbeer 2x125g")
                                .add("ArticleList", Json.createArrayBuilder()
                                                .add(Json.createObjectBuilder()
                                                                .add("Price", 1.2)
                                                                .add("Quantity", 1.0)
                                                                .add("Discount", 0.0)
                                                                .add("Total", 1.2)))
                                .build();
        }

        public String constructPayload(String question) {
                JsonObject model = getModel();
                String systemContent = "You are an assistant that provides information in JSON format. Please adhere strictly to the following structure: "
                                + model.toString();
                return constructGPTMessages(question, systemContent);
        }

        private String constructGPTMessages(String userContent, String systemContent) {
                JsonObject messageSystem = Json.createObjectBuilder()
                                .add("role", "system")
                                .add("content", systemContent)
                                .build();

                JsonObject messageUser = Json.createObjectBuilder()
                                .add("role", "user")
                                .add("content", userContent)
                                .build();

                JsonArrayBuilder messages = Json.createArrayBuilder()
                                .add(messageSystem)
                                .add(messageUser);

                JsonObject payload = Json.createObjectBuilder()
                                .add("model", modelDefault)
                                .add("messages", messages)
                                .add("temperature", temperature)
                                .build();

                return payload.toString();
        }
}
