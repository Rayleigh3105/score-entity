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
    @Transactional
    fun getOne(@PathParam("id") id: Long): Settings {
        var settings = settingsRepository.findById(id);

        if (settings == null) {
            settingsRepository.persist(Settings())
            settings = settingsRepository.findById(id);
        }

        return settings ?: throw NotFoundException("Settings not found")
    }

    @POST
    @Transactional
    fun create(setting: SettingsUpdateRequest): Settings {
        val newSetting = Settings(
            endTime = setting.endTime,
            price = setting.price,
        )
        settingsRepository.persist(newSetting)
        return newSetting

    }

    @PUT
    @Transactional
    fun update(updated: SettingsUpdateRequest): Settings {
        val setting = settingsRepository.findById(updated.id) ?: throw NotFoundException("Setting not found")
        setting.endTime = updated.endTime
        setting.price = updated.price
        settingsRepository.persist(setting)
        return setting
    }

}