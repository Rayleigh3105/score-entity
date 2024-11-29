package org.rayleigh.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.rayleigh.data.item.ItemUpdateRequest
import org.rayleigh.entity.Item
import org.rayleigh.repository.ItemRepository
import org.rayleigh.service.ItemService

@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ItemResource {

    @Inject
    lateinit var itemRepository: ItemRepository

    @Inject
    lateinit var itemService: ItemService

    @GET
    fun getAllItems(): List<Item> = itemRepository.listAll()

    @POST
    @Transactional
    fun addItem(item: Item): Item {
        itemRepository.persist(item)
        return item
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun deleteItem(@PathParam("id") id: Long): Response {
        val item = itemRepository.findById(id)
        if (item != null) {
            itemRepository.delete(item)
            return Response.noContent().build() // Status 204 (No Content)
        }
        return Response.status(Response.Status.NOT_FOUND)
            .entity(mapOf("error" to "Item with ID $id not found"))
            .build()
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateItem(@PathParam("id") id: Long, request: ItemUpdateRequest): Response {
        return try {
            val updatedItem = itemService.updateItem(id, request)
            Response.ok(updatedItem).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}