package org.rayleigh.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.rayleigh.data.area.AreaUpdateRequest
import org.rayleigh.entity.Area
import org.rayleigh.repository.AreaRepository
import org.rayleigh.service.AreaService

@Path("/areas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AreaResource {

    @Inject
    lateinit var areaRepository: AreaRepository

    @Inject
    lateinit var areaService: AreaService

    @GET
    fun getAllAreas(): List<Area> = areaRepository.listAll()

    @POST
    @Transactional
    fun addItem(item: Area): Area {
        areaRepository.persist(item)
        return item
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun deleteArea(@PathParam("id") id: Long): Response {
        val item = areaRepository.findById(id)
        if (item != null) {
            areaRepository.delete(item)
            return Response.noContent().build() // Status 204 (No Content)
        }
        return Response.status(Response.Status.NOT_FOUND)
            .entity(mapOf("error" to "Area with ID $id not found"))
            .build()
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateArea(@PathParam("id") id: Long, request: AreaUpdateRequest): Response {
        return try {
            val updatedItem = areaService.updateItem(id, request)
            Response.ok(updatedItem).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}