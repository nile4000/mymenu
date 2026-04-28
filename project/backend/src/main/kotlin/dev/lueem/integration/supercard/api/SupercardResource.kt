package dev.lueem.integration.supercard.api

import dev.lueem.integration.supercard.app.SupercardService
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/api/integrations/supercard")
@Produces(MediaType.APPLICATION_JSON)
class SupercardResource @Inject constructor(
    private val supercardService: SupercardService
) {

    @POST
    @Path("session")
    @Consumes(MediaType.APPLICATION_JSON)
    fun setSession(request: SupercardSessionRequest): Response =
        Response.ok(supercardService.setSession(request)).build()

    @GET
    @Path("status")
    fun status(): Response = Response.ok(supercardService.getStatus()).build()

    @POST
    @Path("sync")
    fun sync(): Response = Response.ok(supercardService.sync()).build()

    @GET
    @Path("available")
    fun available(): Response = Response.ok(supercardService.available()).build()

    @POST
    @Path("sync-single")
    @Consumes(MediaType.APPLICATION_JSON)
    fun syncSingle(request: SupercardSyncSingleRequest): Response =
        Response.ok(supercardService.syncSingle(request)).build()

    @POST
    @Path("sync-available")
    fun syncAvailable(): Response = Response.ok(supercardService.syncAvailable()).build()
}
