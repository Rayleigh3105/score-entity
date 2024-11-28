package org.rayleigh.service

import ScoreboardEntry
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.rayleigh.repository.ScoreRepository

@ApplicationScoped
class ScoreService {

    @Inject
    lateinit var scoreRepository: ScoreRepository

    fun calculateScoreboard(): List<ScoreboardEntry> {
        val scores = scoreRepository.listAll()

        return scores.groupBy { it.group } // Gruppieren nach Gruppe
            .map { (group, groupScores) ->
                ScoreboardEntry(
                    groupId = group?.id ?: -1,
                    name = group?.name ?: "Unknown",
                    totalScore = groupScores.sumOf { it.points },
                    imageUrl = group?.imageUrl
                )
            }
            .sortedByDescending { it.totalScore } // Nach Punkten sortieren
    }
}
