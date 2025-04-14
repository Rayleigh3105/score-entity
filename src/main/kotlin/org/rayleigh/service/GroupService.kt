package org.rayleigh.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.rayleigh.data.group.GroupUpdateRequest
import org.rayleigh.entity.Group
import org.rayleigh.entity.Score
import org.rayleigh.repository.GroupRepository
import org.rayleigh.repository.ScoreRepository
import org.rayleigh.repository.SettingsRepository
import java.time.LocalDateTime

@ApplicationScoped
class GroupService {

    @Inject
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var scoreRepository: ScoreRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Transactional
    fun addPointsToGroup(groupId: Long, points: Int): Score {
        var settings = settingsRepository.findById(1)

        if (settings != null && settings.endTime.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Cannot add points after the end time.")
        }

        // Suche nach der Gruppe
        val group = groupRepository.findById(groupId)
            ?: throw IllegalArgumentException("Group with ID $groupId not found.")

        // Speichere den neuen Score
        val score = Score(group = group, points = points, timestamp = LocalDateTime.now())
        scoreRepository.persist(score)

        return score
    }

    @Transactional
    fun updateGroup(id: Long, request: GroupUpdateRequest): Group {
        val group = groupRepository.findById(id)
            ?: throw IllegalArgumentException("Group with ID $id not found.")

        group.name = request.name.toString()
        group.image = request.image
        groupRepository.persist(group)
        return group
    }

    @Transactional
    fun resetPoints() {
        scoreRepository.deleteAll()
        val groups = groupRepository.listAll()
        for (group in groups) {
            scoreRepository.persist(Score(group = group, points = 0, timestamp = LocalDateTime.now()))
        }
    }

}