package org.rayleigh.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.rayleigh.data.item.ItemUpdateRequest
import org.rayleigh.entity.Item
import org.rayleigh.repository.AreaRepository
import org.rayleigh.repository.DepositRepository
import org.rayleigh.repository.ItemRepository

@ApplicationScoped
class ItemService {

    @Inject
    lateinit var itemRepository: ItemRepository

    @Inject
    lateinit var depositRepository: DepositRepository

    @Inject
    lateinit var areaRepository: AreaRepository

    @Transactional
    fun updateItem(id: Long, request: ItemUpdateRequest): Item {
        val item = itemRepository.findById(id)
            ?: throw IllegalArgumentException("Item with ID $id not found.")

        item.name = request.name.toString()
        item.price = request.price
        item.color = request.color.toString()
        // Aktualisiere Zuordnung zu Bereichen (ManyToMany)
        item.areas.clear()
        request.areaIds.forEach { areaId ->
            val area = areaRepository.findById(areaId)
                ?: throw IllegalArgumentException("Area mit ID $areaId nicht gefunden.")
            item.areas.add(area)
        }
        item.quantity = request.quantity
        val deposit = request.depositId?.let { depositRepository.findById(it) }
        item.deposit = deposit
        itemRepository.persist(item)
        return item
    }
}