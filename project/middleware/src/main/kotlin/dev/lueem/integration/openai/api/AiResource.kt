package dev.lueem.integration.openai.api

import dev.lueem.integration.openai.app.AiGatewayService
import dev.lueem.integration.openai.api.dto.*
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/ai")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AiResource @Inject constructor(private val aiGatewayService: AiGatewayService) {

    @POST
    @Path("extract-unit")
    fun extractUnit(request: ExtractUnitRequest): Response =
        Response.ok(aiGatewayService.extractUnit(request)).build()

    @POST
    @Path("recipe")
    fun recipe(request: RecipeRequest): Response =
        Response.ok(aiGatewayService.recipe(request)).build()
}
