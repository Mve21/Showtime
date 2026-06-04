package rs.edu.raf.rma.movies.favorites

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

class FavoritesViewModel(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: FavoritesContract.UiState.() -> FavoritesContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<FavoritesContract.UiEvent>()

    fun setEvent(event: FavoritesContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<FavoritesContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: FavoritesContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeFavorites()
        sync()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is FavoritesContract.UiEvent.RemoveFavorite -> removeFavorite(event.imdbId)
                    is FavoritesContract.UiEvent.MovieClicked ->
                        setEffect(FavoritesContract.SideEffect.NavigateToDetail(event.imdbId))
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            movieRepository.observeFavorites()
                .collect { movies -> setState { copy(movies = movies) } }
        }
    }

    private fun sync(force: Boolean = false) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                movieRepository.syncFavorites(force)
            } catch (e: Exception) {
                Napier.e("syncFavorites failed", e)
                setState { copy(error = "Nije moguće sinhronizovati sa serverom") }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun removeFavorite(imdbId: String) {
        viewModelScope.launch {
            try {
                movieRepository.removeFavorite(imdbId)
            } catch (e: Exception) {
                Napier.e("removeFavorite failed for $imdbId", e)
                setEffect(FavoritesContract.SideEffect.ShowError("Greška pri uklanjanju iz omiljenih"))
            }
        }
    }
}
