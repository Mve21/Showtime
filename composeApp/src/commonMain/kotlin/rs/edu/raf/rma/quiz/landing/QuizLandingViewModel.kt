package rs.edu.raf.rma.quiz.landing

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

class QuizLandingViewModel(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizLandingContract.UiState())
    val state: StateFlow<QuizLandingContract.UiState> = _state.asStateFlow()

    private val _sideEffect = Channel<QuizLandingContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        checkPool()
    }

    fun onEvent(event: QuizLandingContract.UiEvent) {
        when (event) {
            QuizLandingContract.UiEvent.StartClicked -> viewModelScope.launch {
                _sideEffect.send(QuizLandingContract.SideEffect.NavigateToSession)
            }
        }
    }

    private fun checkPool() = viewModelScope.launch {
        _state.update { it.copy(phase = QuizLandingContract.Phase.Checking) }
        if (quizRepository.hasEnoughMoviesForQuiz()) {
            _state.update { it.copy(phase = QuizLandingContract.Phase.Ready) }
            return@launch
        }
        _state.update { it.copy(phase = QuizLandingContract.Phase.Bootstrapping) }
        try {
            quizRepository.bootstrapMovies()
            val ready = quizRepository.hasEnoughMoviesForQuiz()
            _state.update {
                it.copy(
                    phase = if (ready) QuizLandingContract.Phase.Ready
                            else QuizLandingContract.Phase.NotEnoughMovies
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(phase = QuizLandingContract.Phase.NotEnoughMovies) }
        }
    }
}
