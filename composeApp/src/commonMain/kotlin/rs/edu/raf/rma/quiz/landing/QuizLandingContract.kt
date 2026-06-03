package rs.edu.raf.rma.quiz.landing

interface QuizLandingContract {

    enum class Phase { Checking, Bootstrapping, Ready, NotEnoughMovies }

    data class UiState(
        val phase: Phase = Phase.Checking,
    )

    sealed interface UiEvent {
        data object StartClicked : UiEvent
    }

    sealed interface SideEffect {
        data object NavigateToSession : SideEffect
    }
}
