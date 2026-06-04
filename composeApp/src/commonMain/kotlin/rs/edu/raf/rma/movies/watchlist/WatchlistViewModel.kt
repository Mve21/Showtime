package rs.edu.raf.rma.movies.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.MovieRepository

class WatchlistViewModel(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WatchlistContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: WatchlistContract.UiState.() -> WatchlistContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<WatchlistContract.UiEvent>()

    fun setEvent(event: WatchlistContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<WatchlistContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: WatchlistContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeWatchlist()
        sync()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    WatchlistContract.UiEvent.Refresh -> sync(force = true)
                    is WatchlistContract.UiEvent.RemoveFromWatchlist -> removeFromWatchlist(event.imdbId)
                    is WatchlistContract.UiEvent.MovieClicked ->
                        setEffect(WatchlistContract.SideEffect.NavigateToDetail(event.imdbId))
                }
            }
        }
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            movieRepository.observeWatchlist()
                .collect { movies -> setState { copy(movies = movies) } }
        }
    }

    private fun sync(force: Boolean = false) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                movieRepository.syncWatchlist(force)
            } catch (e: Exception) {
                Napier.e("syncWatchlist failed", e)
                setState { copy(error = "Nije moguće sinhronizovati sa serverom") }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun removeFromWatchlist(imdbId: String) {
        viewModelScope.launch {
            try {
                movieRepository.removeFromWatchlist(imdbId)
            } catch (e: Exception) {
                Napier.e("removeFromWatchlist failed for $imdbId", e)
                setEffect(WatchlistContract.SideEffect.ShowError("Greška pri uklanjanju sa liste za gledanje"))
            }
        }
    }
}
