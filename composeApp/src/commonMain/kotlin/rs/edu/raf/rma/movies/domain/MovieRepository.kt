package rs.edu.raf.rma.movies.domain

import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun observeMovies(
        genreId: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minRating: Float? = null,
        query: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
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

    fun observeMovieDetail(imdbId: String): Flow<MovieDetail?>

    suspend fun fetchMovieDetail(imdbId: String)

    fun observeGenres(): Flow<List<Genre>>

    suspend fun refreshGenres()

    fun observeFavorites(): Flow<List<Movie>>

    fun observeFavoriteCount(): Flow<Int>

    suspend fun syncFavorites(force: Boolean = false)

    suspend fun addFavorite(imdbId: String)

    suspend fun removeFavorite(imdbId: String)

    fun observeWatchlist(): Flow<List<Movie>>

    fun observeWatchlistCount(): Flow<Int>

    suspend fun syncWatchlist(force: Boolean = false)

    suspend fun addToWatchlist(imdbId: String)

    suspend fun removeFromWatchlist(imdbId: String)

    suspend fun bootstrapCatalog()
}
