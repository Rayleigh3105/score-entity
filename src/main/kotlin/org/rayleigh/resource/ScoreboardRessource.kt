package org.rayleigh.resource

import ScoreboardEntry
import io.smallrye.common.annotation.Blocking
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

    @Blocking
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun streamScoreboard(): Flow.Publisher<String> {
        val initial = scoreService.calculateScoreboard()
        val initialJson = toJson(initial)

        return Multi.createBy().merging()
            .streams(
                Multi.createFrom().item(initialJson),
                Multi.createFrom().ticks().every(java.time.Duration.ofSeconds(1))
                    .onItem().transform {
                        toJson(scoreService.calculateScoreboard())
                    }
            )
            .runSubscriptionOn(managedExecutor)
    }

    private fun toJson(scoreboard: List<ScoreboardEntry>): String {
        return kotlinx.serialization.json.Json.encodeToString(scoreboard)
    }
}