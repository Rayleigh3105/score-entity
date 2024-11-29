package org.rayleigh.resource

import ScoreboardEntry
import io.smallrye.mutiny.Multi
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import kotlinx.serialization.encodeToString
import org.eclipse.microprofile.context.ManagedExecutor
import org.rayleigh.service.ScoreService
import java.util.concurrent.Flow

@Path("/scoreboard-stream")
class ScoreboardRessource {

    @Inject
    lateinit var scoreService: ScoreService

    @Inject
    lateinit var managedExecutor: ManagedExecutor

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun streamScoreboard(): Flow.Publisher<String> {
        return Multi.createFrom().ticks().every(java.time.Duration.ofSeconds(1)) // Alle 1 Sekunde
            .onItem().transform {
                val scoreboard = scoreService.calculateScoreboard()
                toJson(scoreboard) // Konvertiere die Liste in JSON
            }
            .runSubscriptionOn(managedExecutor)
    }

    private fun toJson(scoreboard: List<ScoreboardEntry>): String {
        return kotlinx.serialization.json.Json.encodeToString(scoreboard)
    }
}