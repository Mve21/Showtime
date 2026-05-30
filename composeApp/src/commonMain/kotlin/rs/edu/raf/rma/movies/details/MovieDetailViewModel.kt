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
}
