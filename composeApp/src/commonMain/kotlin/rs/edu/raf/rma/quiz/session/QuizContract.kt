package rs.edu.raf.rma.quiz.session

import rs.edu.raf.rma.quiz.domain.QuizQuestion

interface QuizContract {

    data class UiState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val questions: List<QuizQuestion> = emptyList(),
        val currentIndex: Int = 0,
        val selectedOptionIndex: Int? = null,
        val isFeedbackPhase: Boolean = false,
        val timeRemainingSeconds: Int = 60,
        val correctCount: Int = 0,
        val showAbandonDialog: Boolean = false,
    ) {
        val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)
        val totalQuestions: Int get() = questions.size
    }

    sealed interface UiEvent {
        data class AnswerSelected(val optionIndex: Int) : UiEvent
        data object BackPressed : UiEvent
        data object AbandonConfirmed : UiEvent
        data object AbandonDismissed : UiEvent
    }

    sealed interface SideEffect {
        data object NavigateToResult : SideEffect
        data object NavigateBack : SideEffect
    }
}
