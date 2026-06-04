package rs.edu.raf.rma.movies.watchlist

import rs.edu.raf.rma.movies.domain.Movie

interface WatchlistContract {

    data class UiState(
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data class RemoveFromWatchlist(val imdbId: String) : UiEvent()
        data class MovieClicked(val imdbId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToDetail(val imdbId: String) : SideEffect()
        data class ShowError(val message: String) : SideEffect()
    }
}
