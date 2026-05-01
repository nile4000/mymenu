package dev.lueem.categorization.api

import dev.lueem.categorization.api.dto.CategorizeRequest
import dev.lueem.categorization.api.dto.Category
import dev.lueem.categorization.infra.CategorizationClient
import dev.lueem.categorization.infra.CategoryCatalog
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
class CategoryResource @Inject constructor(
    private val categoryCatalog: CategoryCatalog,
    private val categorizationClient: CategorizationClient
) {

    @GET
    fun list(): Response {
        val dtos = categoryCatalog.all.map { Category(it.name, it.icon, it.color) }
        return Response.ok(dtos).build()
    }

    @POST
    @Path("categorize")
    @Consumes(MediaType.APPLICATION_JSON)
    fun categorize(request: CategorizeRequest): Response {
        require(request.items.isNotEmpty()) { "Die Liste darf nicht leer sein" }
        return Response.ok(categorizationClient.categorize(request)).build()
    }
}
