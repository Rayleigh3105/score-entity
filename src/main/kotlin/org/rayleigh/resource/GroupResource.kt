package org.rayleigh.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.rayleigh.data.group.GroupUpdateRequest
import org.rayleigh.entity.Group
import org.rayleigh.repository.GroupRepository
import org.rayleigh.service.GroupService

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GroupResource {

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var groupService: GroupService

    @GET
    fun getAllGroups(): List<Group> = groupRepository.listAll()

    @POST
    @Transactional
    fun addGroup(group: Group): Group {
        groupRepository.persist(group)
        return group
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun deleteGroup(@PathParam("id") id: Long): Response {
        val group = groupRepository.findById(id)
        if (group != null) {
            groupRepository.delete(group)
            return Response.noContent().build() // Status 204 (No Content)
        }
        return Response.status(Response.Status.NOT_FOUND)
            .entity(mapOf("error" to "Group with ID $id not found"))
            .build()
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateGroup(@PathParam("id") id: Long, request: GroupUpdateRequest): Response {
        return try {
            groupService.updateGroup(id, request)
            Response.ok(mapOf("message" to "Group updated successfully")).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}