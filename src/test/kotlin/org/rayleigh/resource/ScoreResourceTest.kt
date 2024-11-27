package org.rayleigh.resource

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.transaction.Transactional
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.rayleigh.entity.Group

import jakarta.inject.Inject
import org.rayleigh.repository.GroupRepository

@QuarkusTest
class ScoreResourceTest {

    @Inject
    lateinit var groupRepository: GroupRepository

    @Test
    @Transactional
    fun `should add points to group via API`() {
        // Arrange: Create and persist a test group
        val group = Group(name = "Test Group", totalScore = 10)
        groupRepository.persist(group) // Use injected repository to persist

        // Act & Assert
        RestAssured.given()
            .contentType("application/json")
            .body("""{"groupId": ${group.id}, "points": 5}""")
            .`when`()
            .post("/scores")
            .then()
            .statusCode(200)
            .body("message", Matchers.equalTo("Points added successfully"))

        // Verify: Check if points are updated
        val updatedGroup = group.id?.let { groupRepository.findById(it) }
        assertEquals(15, updatedGroup?.totalScore)
    }


    @Test
    fun `should return 404 if group not found via API`() {
        // Act & Assert
        RestAssured.given()
            .contentType("application/json")
            .body("""{"groupId": 999, "points": 5}""") // ID 999 existiert nicht
            .`when`()
            .post("/scores")
            .then()
            .statusCode(404)
            .body("error", Matchers.equalTo("Group with ID 999 not found."))
    }
}