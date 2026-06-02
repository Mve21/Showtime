package rs.edu.raf.rma.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.edu.raf.rma.auth.AuthRepository
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.movies.domain.MovieRepository
import rs.edu.raf.rma.quiz.QuizRepository

class ProfileViewModel(
    private val authStore: AuthStore,
    private val authRepository: AuthRepository,
    private val movieRepository: MovieRepository,
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.UiState())
    val state: StateFlow<ProfileContract.UiState> = _state.asStateFlow()

    private val _sideEffect = Channel<ProfileContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        observeUserData()
        observeStats()
    }

    fun onEvent(event: ProfileContract.UiEvent) {
        when (event) {
            ProfileContract.UiEvent.LogoutClicked -> logout()
        }
    }

    private fun observeUserData() {
        authStore.authState
            .onEach { authState ->
                if (authState is AuthState.Authenticated) {
                    _state.update {
                        it.copy(
                            username = authState.data.username ?: "",
                            fullName = authState.data.fullName ?: "",
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeStats() {
        combine(
            movieRepository.observeFavoriteCount(),
            movieRepository.observeWatchlistCount(),
            quizRepository.observeBestScore(),
            quizRepository.observeTotalPlays(),
        ) { favCount, watchCount, bestScore, totalPlays ->
            _state.update {
                it.copy(
                    favoriteCount = favCount,
                    watchlistCount = watchCount,
                    bestScore = bestScore,
                    totalPlays = totalPlays,
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.logout()
            _sideEffect.send(ProfileContract.SideEffect.NavigateToAuth)
        }
    }
}
