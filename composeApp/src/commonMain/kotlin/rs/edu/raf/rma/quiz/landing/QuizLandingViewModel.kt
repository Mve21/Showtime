package rs.edu.raf.rma.quiz.landing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.quiz.QuizRepository

class QuizLandingViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizLandingContract.UiState())
    val state: StateFlow<QuizLandingContract.UiState> = _state.asStateFlow()
    private fun setState(reducer: QuizLandingContract.UiState.() -> QuizLandingContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<QuizLandingContract.UiEvent>()
    fun setEvent(event: QuizLandingContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<QuizLandingContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: QuizLandingContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        checkPool()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    QuizLandingContract.UiEvent.StartClicked ->
                        setEffect(QuizLandingContract.SideEffect.NavigateToSession)
                }
            }
        }
    }

    private fun checkPool() = viewModelScope.launch {
        setState { copy(phase = QuizLandingContract.Phase.Checking) }
        if (quizRepository.hasEnoughMoviesForQuiz()) {
            setState { copy(phase = QuizLandingContract.Phase.Ready) }
            return@launch
        }
        setState { copy(phase = QuizLandingContract.Phase.Bootstrapping) }
        try {
            quizRepository.bootstrapMovies()
            val ready = quizRepository.hasEnoughMoviesForQuiz()
            setState {
                copy(
                    phase = if (ready) QuizLandingContract.Phase.Ready
                            else QuizLandingContract.Phase.NotEnoughMovies,
                )
            }
        } catch (e: Exception) {
            setState { copy(phase = QuizLandingContract.Phase.NotEnoughMovies) }
        }
    }
}
