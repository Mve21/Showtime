package rs.edu.raf.rma.splash

interface SplashContract {

    data class UiState(
        val bootState: BootState = BootState.Loading,
        val isLoggedIn: Boolean = false,
    )

    sealed class UiEvent {
        data object Retry : UiEvent()
    }
}
