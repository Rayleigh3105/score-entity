package org.rayleigh.resource

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.rayleigh.data.settings.SettingsUpdateRequest
import org.rayleigh.entity.Settings
import org.rayleigh.repository.SettingsRepository

@Path("/settings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class SettingsResource {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @GET
    @Path("/{id}")
    fun getOne(@PathParam("id") id: Long): Settings =
        settingsRepository.findById(id) ?: throw NotFoundException("Setting not found")

    @POST
    @Transactional
    fun create(setting: SettingsUpdateRequest): Settings {
        val newSetting = Settings(
            endTime = setting.endTime,
        )
        settingsRepository.persist(newSetting)
        return newSetting

    }

    @PUT
    @Transactional
    fun update(updated: SettingsUpdateRequest): Settings {
        val setting = settingsRepository.findById(updated.id) ?: throw NotFoundException("Setting not found")
        setting.endTime = updated.endTime
        settingsRepository.persist(setting)
        return setting
    }

}