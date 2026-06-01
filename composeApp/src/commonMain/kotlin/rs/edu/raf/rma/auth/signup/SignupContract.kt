package rs.edu.raf.rma.auth.signup

object SignupContract {

    data class UiState(
        val fullName: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface UiEvent {
        data class FullNameChanged(val value: String) : UiEvent
        data class UsernameChanged(val value: String) : UiEvent
        data class PasswordChanged(val value: String) : UiEvent
        data object SignupClicked : UiEvent
    }

    sealed interface SideEffect {
        data object NavigateToMovies : SideEffect
    }
}
