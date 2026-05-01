package dev.lueem.category.api

import dev.lueem.category.api.dto.CategoryDto
import dev.lueem.category.domain.Categories
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/categories")
class CategoryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun list(): Response {
        val dtos = Categories.ALL.map { CategoryDto(it.name, it.icon, it.color) }
        return Response.ok(dtos).build()
    }
}
