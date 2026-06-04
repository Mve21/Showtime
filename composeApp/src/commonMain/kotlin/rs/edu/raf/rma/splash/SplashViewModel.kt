package rs.edu.raf.rma.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.movies.domain.MovieRepository

class SplashViewModel(
    private val authStore: AuthStore,
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SplashContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: SplashContract.UiState.() -> SplashContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<SplashContract.UiEvent>()

    fun setEvent(event: SplashContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
        checkAuthState()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    SplashContract.UiEvent.Retry -> {
                        setState { copy(bootState = BootState.Loading) }
                        checkAuthState()
                    }
                }
            }
        }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val authState = authStore.awaitInitialAuthState()
                setState {
                    copy(
                        isLoggedIn = authState is AuthState.Authenticated,
                        bootState = BootState.Success,
                    )
                }
                if (authState is AuthState.Authenticated) {
                    viewModelScope.launch {
                        runCatching { movieRepository.bootstrapCatalog() }
                    }
                }
            } catch (e: Exception) {
                setState { copy(bootState = BootState.Failed(e)) }
            }
        }
    }
}
