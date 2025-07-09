package org.rayleigh.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.rayleigh.data.deposit.DepositUpdateRequest
import org.rayleigh.entity.Deposit
import org.rayleigh.repository.DepositRepository
import org.rayleigh.service.DepositService

@Path("/deposits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class DepositResource {

    @Inject
    lateinit var depositRepository: DepositRepository

    @Inject
    lateinit var depositService: DepositService

    @GET
    fun getAllDeposits(): List<Deposit> = depositRepository.listAll()

    @POST
    @Transactional
    fun addDeposit(deposit: Deposit): Deposit {
        depositRepository.persist(deposit)
        return deposit
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun deleteDeposit(@PathParam("id") id: Long): Response {
        val deposit = depositRepository.findById(id)
        if (deposit != null) {
            depositRepository.delete(deposit)
            return Response.noContent().build() // Status 204 (No Content)
        }
        return Response.status(Response.Status.NOT_FOUND)
            .entity(mapOf("error" to "Deposit with ID $id not found"))
            .build()
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateDeposit(@PathParam("id") id: Long, request: DepositUpdateRequest): Response {
        return try {
            val updatedDeposit = depositService.updateDeposit(id, request)
            Response.ok(updatedDeposit).build()
        } catch (e: IllegalArgumentException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }
}