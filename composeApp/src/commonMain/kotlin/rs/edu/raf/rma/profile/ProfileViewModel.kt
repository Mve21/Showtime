package rs.edu.raf.rma.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
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
    val state = _state.asStateFlow()

    private fun setState(reducer: ProfileContract.UiState.() -> ProfileContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<ProfileContract.UiEvent>()

    fun setEvent(event: ProfileContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<ProfileContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: ProfileContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeUserData()
        observeStats()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    ProfileContract.UiEvent.LogoutClicked -> logout()
                }
            }
        }
    }

    private fun observeUserData() {
        viewModelScope.launch {
            authStore.authState.collect { authState ->
                if (authState is AuthState.Authenticated) {
                    setState {
                        copy(
                            username = authState.data.username ?: "",
                            fullName = authState.data.fullName ?: "",
                        )
                    }
                }
            }
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            combine(
                movieRepository.observeFavoriteCount(),
                movieRepository.observeWatchlistCount(),
                quizRepository.observeBestScore(),
                quizRepository.observeTotalPlays(),
            ) { favCount, watchCount, bestScore, totalPlays ->
                favCount to Triple(watchCount, bestScore, totalPlays)
            }.collect { (favCount, rest) ->
                val (watchCount, bestScore, totalPlays) = rest
                setState {
                    copy(
                        favoriteCount = favCount,
                        watchlistCount = watchCount,
                        bestScore = bestScore,
                        totalPlays = totalPlays,
                    )
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            authRepository.logout()
            setEffect(ProfileContract.SideEffect.NavigateToAuth)
        }
    }
}
