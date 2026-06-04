package rs.edu.raf.rma.quiz.result

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

class QuizResultViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizResultContract.UiState())
    val state: StateFlow<QuizResultContract.UiState> = _state.asStateFlow()
    private fun setState(reducer: QuizResultContract.UiState.() -> QuizResultContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<QuizResultContract.UiEvent>()
    fun setEvent(event: QuizResultContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<QuizResultContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: QuizResultContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        loadResult()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    QuizResultContract.UiEvent.PlayAgain ->
                        setEffect(QuizResultContract.SideEffect.NavigateToSession)
                    QuizResultContract.UiEvent.GoHome ->
                        setEffect(QuizResultContract.SideEffect.NavigateToHome)
                }
            }
        }
    }

    private fun loadResult() = viewModelScope.launch {
        val session = quizRepository.getLastSession()
        setState { copy(session = session, isLoading = false) }
    }
}
