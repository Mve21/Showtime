package rs.edu.raf.rma.auth.login

object LoginContract {

    data class UiState(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface UiEvent {
        data class UsernameChanged(val value: String) : UiEvent
        data class PasswordChanged(val value: String) : UiEvent
        data object LoginClicked : UiEvent
        data object SignupClicked : UiEvent
    }

    sealed interface SideEffect {
        data object NavigateToMovies : SideEffect
        data object NavigateToSignup : SideEffect
    }
}
