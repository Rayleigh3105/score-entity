package org.rayleigh.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.rayleigh.data.group.GroupUpdateRequest
import org.rayleigh.data.item.ItemUpdateRequest
import org.rayleigh.entity.Group
import org.rayleigh.entity.Item
import org.rayleigh.repository.ItemRepository

@ApplicationScoped
class ItemService {

    @Inject
    lateinit var itemRepository: ItemRepository

    @Transactional
    fun updateItem(id: Long, request: ItemUpdateRequest): Item {
        val item = itemRepository.findById(id)
            ?: throw IllegalArgumentException("Item with ID $id not found.")

        item.name = request.name.toString()
        item.scoreValue = request.scoreValue
        item.image = request.image
        itemRepository.persist(item)
        return item
    }
}