package dev.lueem.ai.api

import dev.lueem.ai.app.AiGatewayService
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/ai")
class AiResource @Inject constructor(private val aiGatewayService: AiGatewayService) {

    @POST
    @Path("categorize")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun categorize(request: CategorizeRequest): Response =
        Response.ok(aiGatewayService.categorize(request)).build()

    @POST
    @Path("extract-unit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun extractUnit(request: ExtractUnitRequest): Response =
        Response.ok(aiGatewayService.extractUnit(request)).build()

    @POST
    @Path("recipe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun recipe(request: RecipeRequest): Response =
        Response.ok(aiGatewayService.recipe(request)).build()
}
