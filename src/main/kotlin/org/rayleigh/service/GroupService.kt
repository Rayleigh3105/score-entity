package org.rayleigh.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.rayleigh.entity.Group
import org.rayleigh.repository.GroupRepository

@ApplicationScoped
class GroupService {

    @Inject
    lateinit var groupRepository: GroupRepository

    @Transactional
    fun addPointsToGroup(groupId: Long, points: Int) {
        val group = groupRepository.findById(groupId)
            ?: throw IllegalArgumentException("Group with ID $groupId not found.")

        group.totalScore += points
        groupRepository.persist(group) // Änderungen speichern
    }

    fun getGroupById(groupId: Long): Group {
        return groupRepository.findByIdOrThrow(groupId)
    }
}