package org.rayleigh.service

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.rayleigh.data.group.GroupUpdateRequest
import org.rayleigh.entity.Group
import org.rayleigh.repository.GroupRepository
import org.rayleigh.repository.ScoreRepository

@QuarkusTest
class GroupServiceTest {

    @InjectMock
    lateinit var groupRepository: GroupRepository

    @InjectMock
    lateinit var scoreRepository: ScoreRepository

    @Inject
    lateinit var groupService: GroupService

    @Test
    fun `should persist score when adding points to group`() {
        val group = Group(id = 1, name = "Test Group", totalScore = 10)
        Mockito.`when`(groupRepository.findById(1)).thenReturn(group)

        val score = groupService.addPointsToGroup(1, 5)

        assertEquals(5, score.points)
        assertEquals(group, score.group)
        assertNotNull(score.timestamp)
        Mockito.verify(scoreRepository).persist(score)
    }

    @Test
    fun `should throw exception if group not found when adding points`() {
        Mockito.`when`(groupRepository.findById(1)).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            groupService.addPointsToGroup(1, 5)
        }
        assertEquals("Group with ID 1 not found.", exception.message)
    }

    @Test
    fun `should throw exception if points are zero or negative when adding points`() {
        val group = Group(id = 1, name = "Test Group", totalScore = 10)
        Mockito.`when`(groupRepository.findById(1)).thenReturn(group)

        val exceptionZero = assertThrows<IllegalArgumentException> {
            groupService.addPointsToGroup(1, 0)
        }
        assertEquals("Points must be greater than 0.", exceptionZero.message)

        val exceptionNegative = assertThrows<IllegalArgumentException> {
            groupService.addPointsToGroup(1, -5)
        }
        assertEquals("Points must be greater than 0.", exceptionNegative.message)
    }

    @Test
    fun `should update group details`() {
        val group = Group(id = 1, name = "Old Name", imageUrl = "old_url")
        Mockito.`when`(groupRepository.findById(1)).thenReturn(group)
        val request = GroupUpdateRequest(name = "New Name", imageURL = "new_url")

        groupService.updateGroup(1, request)

        assertEquals("New Name", group.name)
        assertEquals("new_url", group.imageUrl)
        Mockito.verify(groupRepository).persist(group)
    }

    @Test
    fun `should throw exception if group not found when updating`() {
        Mockito.`when`(groupRepository.findById(1)).thenReturn(null)
        val request = GroupUpdateRequest(name = "New Name", imageURL = "new_url")

        val exception = assertThrows<IllegalArgumentException> {
            groupService.updateGroup(1, request)
        }
        assertEquals("Group with ID 1 not found.", exception.message)
    }
}