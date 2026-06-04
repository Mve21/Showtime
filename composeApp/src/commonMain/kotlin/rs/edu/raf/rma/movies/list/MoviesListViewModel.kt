package rs.edu.raf.rma.movies.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
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

    private val events = MutableSharedFlow<MoviesListContract.UiEvent>()

    fun setEvent(event: MoviesListContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<MoviesListContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: MoviesListContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
        observeMovies()
        observeGenres()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is MoviesListContract.UiEvent.MovieClicked ->
                        setEffect(MoviesListContract.SideEffect.NavigateToDetail(event.imdbId))
                    is MoviesListContract.UiEvent.SearchQueryChanged ->
                        setState { copy(query = event.query) }
                    MoviesListContract.UiEvent.ToggleFilterSheet ->
                        setState { copy(isFilterSheetVisible = !isFilterSheetVisible) }
                    is MoviesListContract.UiEvent.FilterApplied ->
                        setState {
                            copy(
                                selectedGenreId = event.genreId,
                                minYear = event.minYear,
                                maxYear = event.maxYear,
                                minRating = event.minRating,
                                sortBy = event.sortBy,
                                sortOrder = event.sortOrder,
                                isFilterSheetVisible = false,
                            )
                        }
                    MoviesListContract.UiEvent.FilterCleared ->
                        setState {
                            copy(
                                selectedGenreId = null,
                                minYear = null,
                                maxYear = null,
                                minRating = null,
                                sortBy = null,
                                sortOrder = null,
                                isFilterSheetVisible = false,
                            )
                        }
                }
            }
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            _state
                .map { it.toFilterParams() }
                .distinctUntilChanged()
                .flatMapLatest { params ->
                    movieRepository.observeMovies(
                        query = params.query,
                        genreId = params.genreId,
                        minYear = params.minYear,
                        maxYear = params.maxYear,
                        minRating = params.minRating,
                        sortBy = params.sortBy,
                        sortOrder = params.sortOrder,
                    )
                }
                .collect { movies -> setState { copy(movies = movies) } }
        }
    }

    private fun observeGenres() {
        viewModelScope.launch {
            movieRepository.observeGenres()
                .distinctUntilChanged()
                .collect { genres -> setState { copy(genres = genres) } }
        }
    }
}

private data class FilterParams(
    val query: String? = null,
    val genreId: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Float? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null,
)

private fun MoviesListContract.UiState.toFilterParams() = FilterParams(
    query = query.takeIf { it.isNotBlank() },
    genreId = selectedGenreId,
    minYear = minYear,
    maxYear = maxYear,
    minRating = minRating,
    sortBy = sortBy,
    sortOrder = sortOrder,
)
