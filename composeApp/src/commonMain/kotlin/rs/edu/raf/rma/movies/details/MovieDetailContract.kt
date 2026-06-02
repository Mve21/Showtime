package rs.edu.raf.rma.movies.details

import rs.edu.raf.rma.movies.domain.MovieDetail

interface MovieDetailContract {

    data class UiState(
        val movieDetail: MovieDetail? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data object BackClicked : UiEvent()
        data object ToggleFavorite : UiEvent()
        data object ToggleWatchlist : UiEvent()
    }

    sealed class SideEffect {
        data object NavigateBack : SideEffect()
        data class ShowError(val message: String) : SideEffect()
    }
}
