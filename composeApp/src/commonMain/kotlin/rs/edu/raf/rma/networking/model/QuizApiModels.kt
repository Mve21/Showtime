package rs.edu.raf.rma.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizResultApiModel(
    val id: Int,
    val category: Int,
    val score: Float,
    @SerialName("played_at") val playedAt: Long,
)

@Serializable
data class PostQuizResultBody(
    val category: Int,
    val score: Float,
)

@Serializable
data class PostQuizResultResponseApiModel(
    val result: QuizResultApiModel,
    val ranking: Int,
)

@Serializable
data class LeaderboardEntryApiModel(
    val rank: Int,
    @SerialName("user_id") val userId: Int,
    val username: String,
    @SerialName("full_name") val fullName: String,
    val score: Float,
    @SerialName("played_at") val playedAt: Long,
    @SerialName("total_plays") val totalPlays: Int,
)
