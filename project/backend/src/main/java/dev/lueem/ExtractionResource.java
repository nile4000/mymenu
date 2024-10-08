package dev.lueem;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;
import dev.lueem.service.ExtractionService;

@Path("/api")
public class ExtractionResource {

    @Inject
    ExtractionService extractionService;

    @POST
    @Path("extract")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractArticlesFromPdf(MultipartFormDataInput input) {
        return extractionService.extractArticles(input);
    }
}
