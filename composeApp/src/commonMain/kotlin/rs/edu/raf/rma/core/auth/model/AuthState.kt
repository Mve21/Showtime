package rs.edu.raf.rma.core.auth.model

sealed class AuthState {
    data object Unauthenticated : AuthState()
    data class Authenticated(val data: AuthData) : AuthState()
}

fun AuthData.asAuthState(): AuthState =
    if (accessToken.isNullOrBlank()) AuthState.Unauthenticated
    else AuthState.Authenticated(this)
