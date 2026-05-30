package rs.edu.raf.rma.movies.domain

import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    // --- Catalog ---

    fun observeMovies(
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Float? = null,
        query: String? = null,
    ): Flow<List<Movie>>

    suspend fun refreshMovies(
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Float? = null,
        query: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        page: Int = 1,
    )

    // --- Movie detail ---

    fun observeMovieDetail(imdbId: String): Flow<MovieDetail?>

    suspend fun refreshMovieDetail(imdbId: String)

    // --- Genres ---

    fun observeGenres(): Flow<List<Genre>>

    suspend fun refreshGenres()

    // --- Favorites ---

    fun observeFavorites(): Flow<List<Movie>>

    fun observeFavoriteCount(): Flow<Int>

    suspend fun syncFavorites()

    suspend fun addFavorite(imdbId: String)

    suspend fun removeFavorite(imdbId: String)

    // --- Watchlist ---

    fun observeWatchlist(): Flow<List<Movie>>

    fun observeWatchlistCount(): Flow<Int>

    suspend fun syncWatchlist()

    suspend fun addToWatchlist(imdbId: String)

    suspend fun removeFromWatchlist(imdbId: String)
}
