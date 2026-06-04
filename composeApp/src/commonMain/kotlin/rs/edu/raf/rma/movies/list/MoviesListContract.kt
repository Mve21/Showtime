package rs.edu.raf.rma.movies.list

import rs.edu.raf.rma.movies.domain.Genre
import rs.edu.raf.rma.movies.domain.Movie

interface MoviesListContract {

    data class UiState(
        val movies: List<Movie> = emptyList(),
        val genres: List<Genre> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val query: String = "",
        val selectedGenreId: Int? = null,
        val minYear: Int? = null,
        val maxYear: Int? = null,
        val minRating: Float? = null,
        val sortBy: String? = null,
        val sortOrder: String? = null,
        val isFilterSheetVisible: Boolean = false,
    ) {
        val hasActiveFilters: Boolean
            get() = selectedGenreId != null || minYear != null || maxYear != null ||
                    minRating != null || sortBy != null
    }

    sealed class UiEvent {
        data class MovieClicked(val imdbId: String) : UiEvent()
        data class SearchQueryChanged(val query: String) : UiEvent()
        data object ToggleFilterSheet : UiEvent()
        data class FilterApplied(
            val genreId: Int?,
            val minYear: Int?,
            val maxYear: Int?,
            val minRating: Float?,
            val sortBy: String?,
            val sortOrder: String?,
        ) : UiEvent()
        data object FilterCleared : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToDetail(val imdbId: String) : SideEffect()
    }
}
