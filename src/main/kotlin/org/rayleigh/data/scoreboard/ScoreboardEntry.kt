import kotlinx.serialization.Serializable

@Serializable
data class ScoreboardEntry(
    val name: String,
    val totalScore: Int,
    val imageUrl: String?
)