package rs.edu.raf.rma.movies.watchlist

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

class WatchlistViewModel(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistContract.UiState())
    val state: StateFlow<WatchlistContract.UiState> = _state.asStateFlow()

    private val _sideEffect = Channel<WatchlistContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        observeWatchlist()
        sync()
    }

    fun onEvent(event: WatchlistContract.UiEvent) {
        when (event) {
            WatchlistContract.UiEvent.Refresh -> sync()
            is WatchlistContract.UiEvent.RemoveFromWatchlist -> removeFromWatchlist(event.imdbId)
            is WatchlistContract.UiEvent.MovieClicked -> viewModelScope.launch {
                _sideEffect.send(WatchlistContract.SideEffect.NavigateToDetail(event.imdbId))
            }
        }
    }

    private fun observeWatchlist() {
        movieRepository.observeWatchlist()
            .onEach { movies -> _state.update { it.copy(movies = movies) } }
            .launchIn(viewModelScope)
    }

    private fun sync() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                movieRepository.syncWatchlist()
            } catch (e: Exception) {
                Napier.e("syncWatchlist failed", e)
                _state.update { it.copy(error = "Nije moguće sinhronizovati sa serverom") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun removeFromWatchlist(imdbId: String) {
        viewModelScope.launch {
            try {
                movieRepository.removeFromWatchlist(imdbId)
            } catch (e: Exception) {
                Napier.e("removeFromWatchlist failed for $imdbId", e)
                _sideEffect.send(WatchlistContract.SideEffect.ShowError("Greška pri uklanjanju sa liste za gledanje"))
            }
        }
    }
}
