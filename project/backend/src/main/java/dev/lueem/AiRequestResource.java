package dev.lueem;

import dev.lueem.clients.OpenAiClient;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
public class AiRequestResource {

    @Inject
    OpenAiClient openAiClient;

    private JsonArray getAnswerOpenAI(JsonObject request) {
        String questionPrefix = request.toString();
        return openAiClient.askQuestion(questionPrefix);
    }

    @POST
    @Path("ai-request")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response aiRequest(JsonObject request) {

        try {
            JsonArray response = getAnswerOpenAI(request);
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }

    }

}
