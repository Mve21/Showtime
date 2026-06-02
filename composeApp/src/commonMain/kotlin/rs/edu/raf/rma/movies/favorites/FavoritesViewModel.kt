package rs.edu.raf.rma.movies.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.MovieRepository

class FavoritesViewModel(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesContract.UiState())
    val state: StateFlow<FavoritesContract.UiState> = _state.asStateFlow()

    private val _sideEffect = Channel<FavoritesContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        observeFavorites()
        sync()
    }

    fun onEvent(event: FavoritesContract.UiEvent) {
        when (event) {
            FavoritesContract.UiEvent.Refresh -> sync()
            is FavoritesContract.UiEvent.RemoveFavorite -> removeFavorite(event.imdbId)
            is FavoritesContract.UiEvent.MovieClicked -> viewModelScope.launch {
                _sideEffect.send(FavoritesContract.SideEffect.NavigateToDetail(event.imdbId))
            }
        }
    }

    private fun observeFavorites() {
        movieRepository.observeFavorites()
            .onEach { movies -> _state.update { it.copy(movies = movies) } }
            .launchIn(viewModelScope)
    }

    private fun sync() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                movieRepository.syncFavorites()
            } catch (e: Exception) {
                Napier.e("syncFavorites failed", e)
                _state.update { it.copy(error = "Nije moguće sinhronizovati sa serverom") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun removeFavorite(imdbId: String) {
        viewModelScope.launch {
            try {
                movieRepository.removeFavorite(imdbId)
            } catch (e: Exception) {
                Napier.e("removeFavorite failed for $imdbId", e)
                _sideEffect.send(FavoritesContract.SideEffect.ShowError("Greška pri uklanjanju iz omiljenih"))
            }
        }
    }
}
