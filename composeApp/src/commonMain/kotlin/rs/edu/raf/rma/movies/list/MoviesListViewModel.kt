package rs.edu.raf.rma.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movies.domain.MovieRepository

class MoviesListViewModel(
    private val movieRepository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MoviesListContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: MoviesListContract.UiState.() -> MoviesListContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    init {
        observeMovies()
        refresh()
    }

    fun setEvent(event: MoviesListContract.UiEvent) {
        when (event) {
            MoviesListContract.UiEvent.Refresh -> refresh()
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            movieRepository.observeMovies()
                .distinctUntilChanged()
                .collect { movies ->
                    setState { copy(movies = movies, error = null) }
                }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            try {
                movieRepository.refreshMovies()
            } catch (e: Exception) {
                setState { copy(error = e.message ?: "Greška pri učitavanju filmova") }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}
