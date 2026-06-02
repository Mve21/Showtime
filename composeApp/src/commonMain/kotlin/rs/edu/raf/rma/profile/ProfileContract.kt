package rs.edu.raf.rma.profile

interface ProfileContract {

    data class UiState(
        val username: String = "",
        val fullName: String = "",
        val favoriteCount: Int = 0,
        val watchlistCount: Int = 0,
        val bestScore: Float? = null,
        val totalPlays: Int = 0,
        val isLoading: Boolean = false,
    )

    sealed class UiEvent {
        data object LogoutClicked : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateToAuth : SideEffect()
    }
}
