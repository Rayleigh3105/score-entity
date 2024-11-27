package org.rayleigh.service

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.rayleigh.entity.Group
import org.rayleigh.repository.GroupRepository

@QuarkusTest
class GroupServiceTest {

    @InjectMock
    lateinit var groupRepository: GroupRepository

    @Inject
    lateinit var groupService: GroupService

    @Test
    fun `should add points to group`() {
        // Arrange
        val group = Group(id = 1, name = "Test Group", totalScore = 10)
        Mockito.`when`(groupRepository.findById(1)).thenReturn(group)

        // Act
        groupService.addPointsToGroup(1, 5)

        // Assert
        assertEquals(15, group.totalScore)
        Mockito.verify(groupRepository).persist(group)
    }

    @Test
    fun `should throw exception if group not found`() {
        // Arrange
        Mockito.`when`(groupRepository.findById(1)).thenReturn(null)

        // Act & Assert: Prüfen, ob die richtige Ausnahme geworfen wird
        val exception = assertThrows<IllegalArgumentException> {
            groupService.addPointsToGroup(1, 5)
        }
        assertEquals("Group with ID 1 not found.", exception.message)
    }

}