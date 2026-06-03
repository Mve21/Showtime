package rs.edu.raf.rma.quiz.result

import rs.edu.raf.rma.quiz.db.QuizSessionEntity

interface QuizResultContract {

    data class UiState(
        val session: QuizSessionEntity? = null,
        val isLoading: Boolean = true,
    )

    sealed interface UiEvent {
        data object PlayAgain : UiEvent
        data object GoHome : UiEvent
    }

    sealed interface SideEffect {
        data object NavigateToSession : SideEffect
        data object NavigateToHome : SideEffect
    }
}
