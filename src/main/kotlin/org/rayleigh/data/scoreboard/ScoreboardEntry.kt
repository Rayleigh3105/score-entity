import kotlinx.serialization.Serializable

@Serializable
data class ScoreboardEntry(
    val groupId: Long,
    val name: String,
    val totalScore: Int,
    val imageUrl: String?
)