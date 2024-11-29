package org.rayleigh.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.rayleigh.data.group.GroupUpdateRequest
import org.rayleigh.entity.Score
import org.rayleigh.repository.GroupRepository
import org.rayleigh.repository.ScoreRepository
import java.time.LocalDateTime

@ApplicationScoped
class GroupService {

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var scoreRepository: ScoreRepository

    @Transactional
    fun addPointsToGroup(groupId: Long, points: Int): Score {
        if (points <= 0) {
            throw IllegalArgumentException("Points must be greater than 0.")
        }

        // Suche nach der Gruppe
        val group = groupRepository.findById(groupId)
            ?: throw IllegalArgumentException("Group with ID $groupId not found.")

        // Aktualisiere den Gesamtpunktestand der Gruppe
        group.totalScore += points
        groupRepository.persist(group)

        // Speichere den neuen Score
        val score = Score(group = group, points = points, timestamp = LocalDateTime.now())
        scoreRepository.persist(score)

        return score
    }

    @Transactional
    fun updateGroup(id: Long, request: GroupUpdateRequest) {
        val group = groupRepository.findById(id)
            ?: throw IllegalArgumentException("Group with ID $id not found.")

        group.name = request.name.toString()
        group.imageUrl = request.imageURL
        groupRepository.persist(group)
    }

    @Transactional
    fun deleteGroup(id: Long) {
        val group = groupRepository.findById(id)
            ?: throw IllegalArgumentException("Group with ID $id not found.")

        groupRepository.delete(group)
    }

}