package rs.edu.raf.rma.quiz.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import rs.edu.raf.rma.quiz.QuizRepository
import rs.edu.raf.rma.quiz.db.QuizSessionEntity

private const val TOTAL_QUESTIONS = 10
private const val TOTAL_TIME_SECONDS = 60
private const val FEEDBACK_DELAY_MS = 900L

class QuizViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizContract.UiState())
    val state: StateFlow<QuizContract.UiState> = _state.asStateFlow()

    private val _sideEffect = Channel<QuizContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        startSession()
    }

    fun onEvent(event: QuizContract.UiEvent) {
        when (event) {
            is QuizContract.UiEvent.AnswerSelected -> onAnswerSelected(event.optionIndex)
            QuizContract.UiEvent.BackPressed ->
                _state.update { it.copy(showAbandonDialog = true) }
            QuizContract.UiEvent.AbandonConfirmed -> abandon()
            QuizContract.UiEvent.AbandonDismissed ->
                _state.update { it.copy(showAbandonDialog = false) }
        }
    }

    private fun startSession() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val questions = quizRepository.generateSession()
            if (questions.isEmpty()) {
                _state.update { it.copy(isLoading = false, error = "Nema dovoljno filmova za kviz") }
                return@launch
            }
            _state.update { it.copy(isLoading = false, questions = questions) }
            startTimer()
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false, error = "Greška pri generisanju kviza") }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val remaining = _state.value.timeRemainingSeconds - 1
                if (remaining <= 0) {
                    _state.update { it.copy(timeRemainingSeconds = 0) }
                    finishSession()
                    break
                }
                _state.update { it.copy(timeRemainingSeconds = remaining) }
            }
        }
    }

    private fun onAnswerSelected(optionIndex: Int) {
        val state = _state.value
        if (state.isFeedbackPhase || state.selectedOptionIndex != null) return

        val question = state.currentQuestion ?: return
        val isCorrect = optionIndex == question.correctOptionIndex

        _state.update {
            it.copy(
                selectedOptionIndex = optionIndex,
                isFeedbackPhase = true,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
            )
        }

        viewModelScope.launch {
            delay(FEEDBACK_DELAY_MS)
            val nextIndex = _state.value.currentIndex + 1
            if (nextIndex >= TOTAL_QUESTIONS) {
                finishSession()
            } else {
                _state.update {
                    it.copy(
                        currentIndex = nextIndex,
                        selectedOptionIndex = null,
                        isFeedbackPhase = false,
                    )
                }
            }
        }
    }

    private fun finishSession() {
        timerJob?.cancel()
        val state = _state.value
        val timeUsed = TOTAL_TIME_SECONDS - state.timeRemainingSeconds
        val score = calculateScore(state.correctCount, state.timeRemainingSeconds)

        viewModelScope.launch {
            quizRepository.saveSession(
                QuizSessionEntity(
                    score = score,
                    correctCount = state.correctCount,
                    incorrectCount = TOTAL_QUESTIONS - state.correctCount,
                    timeUsedSecs = timeUsed.toLong(),
                    playedAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                )
            )
            _sideEffect.send(QuizContract.SideEffect.NavigateToResult)
        }
    }

    private fun abandon() {
        timerJob?.cancel()
        viewModelScope.launch {
            _sideEffect.send(QuizContract.SideEffect.NavigateBack)
        }
    }

    private fun calculateScore(correctCount: Int, timeRemainingSeconds: Int): Float {
        if (correctCount == 0) return 0f
        val score = correctCount * (9f + timeRemainingSeconds / 60f)
        return minOf(score, 100f)
    }
}
