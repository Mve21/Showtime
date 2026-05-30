package rs.edu.raf.rma.networking.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginBody(
    val username: String,
    val password: String,
)

@Serializable
data class SignupBody(
    val username: String,
    val password: String,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class MovieIdBody(
    val imdbId: String,
)

@Serializable
data class UserApiModel(
    val id: Int,
    val username: String,
    @SerialName("full_name") val fullName: String,
)

@Serializable
data class AuthResponseApiModel(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    val user: UserApiModel,
)
