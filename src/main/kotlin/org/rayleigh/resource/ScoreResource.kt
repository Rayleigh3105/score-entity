package org.rayleigh.resource

import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.rayleigh.data.score.ScoreUpdateRequest
import org.rayleigh.service.GroupService

@Path("/scores")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ScoreResource {

    @Inject
    lateinit var groupService: GroupService


    @POST
    fun addPoints(request: ScoreUpdateRequest): Response {
        return try {
            groupService.addPointsToGroup(request.groupId, request.points)
            Response.ok(mapOf("message" to "Points added successfully")).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}
