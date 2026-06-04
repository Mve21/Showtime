package rs.edu.raf.rma.movies.favorites

import rs.edu.raf.rma.movies.domain.Movie

interface FavoritesContract {

    data class UiState(
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data class RemoveFavorite(val imdbId: String) : UiEvent()
        data class MovieClicked(val imdbId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToDetail(val imdbId: String) : SideEffect()
        data class ShowError(val message: String) : SideEffect()
    }
}
