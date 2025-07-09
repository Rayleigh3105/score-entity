package org.rayleigh.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.rayleigh.data.deposit.DepositUpdateRequest
import org.rayleigh.entity.Area
import org.rayleigh.entity.Deposit
import org.rayleigh.repository.DepositRepository

@ApplicationScoped
class DepositService {

    @Inject
    lateinit var depositRepository: DepositRepository

    @Transactional
    fun updateDeposit(id: Long, request: DepositUpdateRequest): Deposit {
        val deposit = depositRepository.findById(id)
            ?: throw IllegalArgumentException("Item with ID $id not found.")

        deposit.value = request.value
        depositRepository.persist(deposit)
        return deposit
    }
}