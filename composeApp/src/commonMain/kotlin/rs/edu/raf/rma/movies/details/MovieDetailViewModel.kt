package rs.edu.raf.rma.movies.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.MovieRepository

class MovieDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val imdbId: String = checkNotNull(savedStateHandle["imdbId"])

    private val _state = MutableStateFlow(MovieDetailContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MovieDetailContract.UiState.() -> MovieDetailContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<MovieDetailContract.UiEvent>()

    fun setEvent(event: MovieDetailContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _sideEffects = Channel<MovieDetailContract.SideEffect>()
    val sideEffects = _sideEffects.receiveAsFlow()

    init {
        observeEvents()
        observeMovieDetail()
        refresh()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    MovieDetailContract.UiEvent.Refresh -> refresh()
                    MovieDetailContract.UiEvent.BackClicked -> {
                        _sideEffects.send(MovieDetailContract.SideEffect.NavigateBack)
                    }
                    MovieDetailContract.UiEvent.ToggleFavorite -> toggleFavorite()
                    MovieDetailContract.UiEvent.ToggleWatchlist -> toggleWatchlist()
                }
            }
        }
    }

    private fun observeMovieDetail() {
        viewModelScope.launch {
            movieRepository.observeMovieDetail(imdbId)
                .distinctUntilChanged()
                .collect { detail ->
                    setState { copy(movieDetail = detail, error = null) }
                }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            try {
                movieRepository.refreshMovieDetail(imdbId)
            } catch (e: Exception) {
                Napier.e("refreshMovieDetail failed for $imdbId", e)
                setState { copy(error = e.message ?: "Greška pri učitavanju filma") }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun toggleFavorite() {
        val isFavorite = _state.value.movieDetail?.isFavorite ?: return
        viewModelScope.launch {
            try {
                if (isFavorite) movieRepository.removeFavorite(imdbId)
                else movieRepository.addFavorite(imdbId)
            } catch (e: Exception) {
                Napier.e("toggleFavorite failed for $imdbId", e)
                val message = if (isFavorite) "Greška pri uklanjanju iz omiljenih"
                              else "Greška pri dodavanju u omiljene"
                _sideEffects.send(MovieDetailContract.SideEffect.ShowError(message))
            }
        }
    }

    private fun toggleWatchlist() {
        val isInWatchlist = _state.value.movieDetail?.isInWatchlist ?: return
        viewModelScope.launch {
            try {
                if (isInWatchlist) movieRepository.removeFromWatchlist(imdbId)
                else movieRepository.addToWatchlist(imdbId)
            } catch (e: Exception) {
                Napier.e("toggleWatchlist failed for $imdbId", e)
                val message = if (isInWatchlist) "Greška pri uklanjanju sa liste za gledanje"
                              else "Greška pri dodavanju na listu za gledanje"
                _sideEffects.send(MovieDetailContract.SideEffect.ShowError(message))
            }
        }
    }
}
