package org.rayleigh.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.rayleigh.entity.Group
import org.rayleigh.repository.GroupRepository

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class GroupResource {

    @Inject
    lateinit var groupRepository: GroupRepository

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
}