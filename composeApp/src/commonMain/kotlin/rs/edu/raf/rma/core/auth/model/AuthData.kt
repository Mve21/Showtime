package rs.edu.raf.rma.core.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthData(
    val accessToken: String? = null,
    val userId: Int? = null,
    val username: String? = null,
    val fullName: String? = null,
)
