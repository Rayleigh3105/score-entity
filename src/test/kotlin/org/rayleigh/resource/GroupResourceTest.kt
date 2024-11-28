package org.rayleigh.resource

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.rayleigh.data.group.GroupUpdateRequest
import org.rayleigh.entity.Group
import org.rayleigh.repository.GroupRepository

@QuarkusTest
class GroupResourceTest {

    @Inject
    lateinit var groupRepository: GroupRepository

    @BeforeEach
    @Transactional
    fun setup() {
        groupRepository.deleteAll()
        groupRepository.flush()
    }

    @Test
    @Transactional
    fun `should add group successfully`() {
        val group = Group(name = "New Group", totalScore = 0)

        val response = RestAssured.given()
            .contentType("application/json")
            .body(group)
            .`when`()
            .post("/groups")
            .then()
            .statusCode(200)
            .extract().`as`(Group::class.java)

        assertNotNull(response.id)
        assertEquals("New Group", response.name)
        assertEquals(0, response.totalScore)
    }

    @Test
    @Transactional
    fun `should delete group successfully`() {
        val group = Group(name = "Group to Delete", totalScore = 0)
        groupRepository.persist(group)
        groupRepository.flush()

        RestAssured.given()
            .`when`()
            .delete("/groups/${group.id}")
            .then()
            .statusCode(204)

        val deletedGroup = group.id?.let { groupRepository.findById(it) }
        assertNull(deletedGroup)
    }

    @Test
    fun `should return 404 if group not found when deleting`() {
        RestAssured.given()
            .`when`()
            .delete("/groups/999")
            .then()
            .statusCode(404)
            .body("error", Matchers.equalTo("Group with ID 999 not found"))
    }

    @Test
    @Transactional
    fun `should update group successfully`() {
        val group = Group(name = "Old Group", totalScore = 0)
        groupRepository.persist(group)
        val request = GroupUpdateRequest(name = "Updated Group", imageURL = "new_url")

        RestAssured.given()
            .contentType("application/json")
            .body(request)
            .`when`()
            .put("/groups/${group.id}")
            .then()
            .statusCode(200)
            .body("message", Matchers.equalTo("Group updated successfully"))

        val updatedGroup = group.id?.let { groupRepository.findById(it) }
        assertEquals("Updated Group", updatedGroup?.name)
        assertEquals("new_url", updatedGroup?.imageUrl)
    }

    @Test
    fun `should return 404 if group not found when updating`() {
        val request = GroupUpdateRequest(name = "Updated Group", imageURL = "new_url")

        RestAssured.given()
            .contentType("application/json")
            .body(request)
            .`when`()
            .put("/groups/999")
            .then()
            .statusCode(404)
            .body("error", Matchers.equalTo("Group with ID 999 not found."))
    }
}