package org.rayleigh.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.rayleigh.data.area.AreaUpdateRequest
import org.rayleigh.entity.Area
import org.rayleigh.repository.AreaRepository

@ApplicationScoped
class AreaService {

    @Inject
    lateinit var areaRepository: AreaRepository

    @Transactional
    fun updateItem(id: Long, request: AreaUpdateRequest): Area {
        val area = areaRepository.findById(id)
            ?: throw IllegalArgumentException("Item with ID $id not found.")

        area.name = request.name.toString()
        areaRepository.persist(area)
        return area
    }
}