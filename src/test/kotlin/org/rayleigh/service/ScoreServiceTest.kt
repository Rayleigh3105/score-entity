package org.rayleigh.service

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.rayleigh.entity.Group
import org.rayleigh.entity.Score
import org.rayleigh.repository.ScoreRepository

@QuarkusTest
class ScoreServiceTest {

    @Inject
    lateinit var scoreService: ScoreService

    @InjectMock
    lateinit var scoreRepository: ScoreRepository

    @Test
    fun `should calculate scoreboard correctly`() {
        val group1 = Group(id = 1, name = "Group 1", imageUrl = "url1")
        val group2 = Group(id = 2, name = "Group 2", imageUrl = "url2")
        val scores = listOf(
            Score(group = group1, points = 10),
            Score(group = group1, points = 20),
            Score(group = group2, points = 15)
        )
        Mockito.`when`(scoreRepository.listAll()).thenReturn(scores)

        val scoreboard = scoreService.calculateScoreboard()

        assertEquals(2, scoreboard.size)
        assertEquals("Group 1", scoreboard[0].name)
        assertEquals(30, scoreboard[0].totalScore)
        assertEquals("url1", scoreboard[0].imageUrl)
        assertEquals("Group 2", scoreboard[1].name)
        assertEquals(15, scoreboard[1].totalScore)
        assertEquals("url2", scoreboard[1].imageUrl)
    }

    @Test
    fun `should handle empty score list`() {
        Mockito.`when`(scoreRepository.listAll()).thenReturn(emptyList())

        val scoreboard = scoreService.calculateScoreboard()

        assertTrue(scoreboard.isEmpty())
    }

    @Test
    fun `should handle scores with null group`() {
        val scores = listOf(
            Score(group = null, points = 10)
        )
        Mockito.`when`(scoreRepository.listAll()).thenReturn(scores)

        val scoreboard = scoreService.calculateScoreboard()

        assertEquals(1, scoreboard.size)
        assertEquals("Unknown", scoreboard[0].name)
        assertEquals(10, scoreboard[0].totalScore)
        assertNull(scoreboard[0].imageUrl)
    }
}
