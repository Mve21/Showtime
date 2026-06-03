package rs.edu.raf.rma.quiz.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.edu.raf.rma.quiz.QuizRepository

class QuizResultViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizResultContract.UiState())
    val state: StateFlow<QuizResultContract.UiState> = _state.asStateFlow()

    private val _sideEffect = Channel<QuizResultContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        loadResult()
    }

    fun onEvent(event: QuizResultContract.UiEvent) {
        viewModelScope.launch {
            when (event) {
                QuizResultContract.UiEvent.PlayAgain ->
                    _sideEffect.send(QuizResultContract.SideEffect.NavigateToSession)
                QuizResultContract.UiEvent.GoHome ->
                    _sideEffect.send(QuizResultContract.SideEffect.NavigateToHome)
            }
        }
    }

    private fun loadResult() = viewModelScope.launch {
        val session = quizRepository.getLastSession()
        _state.update { it.copy(session = session, isLoading = false) }
    }
}
