package rs.edu.raf.rma.movies.list

import rs.edu.raf.rma.movies.domain.Movie

interface MoviesListContract {

    data class UiState(
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
    }
}
