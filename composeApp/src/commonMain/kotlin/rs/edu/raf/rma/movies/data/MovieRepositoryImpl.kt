package rs.edu.raf.rma.movies.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.movies.domain.Genre
import rs.edu.raf.rma.movies.domain.Movie
import rs.edu.raf.rma.movies.domain.MovieDetail
import rs.edu.raf.rma.movies.domain.MovieRepository

class MovieRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val moviesApi: rs.edu.raf.rma.networking.MoviesApi,
) : MovieRepository {

    private val dao = appDatabase.movieDao()

    override fun observeMovies(
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Float?,
        query: String?,
    ): Flow<List<Movie>> =
        dao.observeMovies(genreId, minYear, maxYear, minRating, query)
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshMovies(
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Float?,
        query: String?,
        sortBy: String?,
        sortOrder: String?,
        page: Int,
    ) {
        val response = moviesApi.getMovies(
            page = page,
            pageSize = 20,
            query = query,
            genreId = genreId,
            minYear = minYear,
            maxYear = maxYear,
            minRating = minRating,
            sortBy = sortBy,
            sortOrder = sortOrder,
        )
        val items = response.items
        dao.upsertGenres(items.flatMap { it.toGenreEntities() }.distinctBy { it.id })
        dao.refreshMovieListTransaction(items.map { it.toMovieEntity() })
        items.firstOrNull()?.let {
            println("DEBUG posterPath: ${it.posterPath}")
        }
        items.forEach { item ->
            dao.replaceMovieGenreLinks(
                movieId = item.imdbId,
                genreIds = item.genres.map { it.id },
            )
        }
    }


    override fun observeMovieDetail(imdbId: String): Flow<MovieDetail?> =
        combine(
            dao.observeMovieDetail(imdbId),
            dao.observeIsFavorite(imdbId),
            dao.observeIsInWatchlist(imdbId),
        ) { detail, isFavorite, isInWatchlist ->
            detail?.toDomain(isFavorite = isFavorite, isInWatchlist = isInWatchlist)
        }

    override suspend fun refreshMovieDetail(imdbId: String) {
        val movie = moviesApi.getMovie(imdbId)
        val cast = moviesApi.getMovieCast(imdbId).items
        dao.refreshMovieDetailTransaction(
            movie = movie.toMovieEntity(),
            detail = movie.toMovieDetailEntity(),
            genres = movie.toGenreEntities(),
            genreIds = movie.genres.map { it.id },
            cast = cast.map { it.toPersonEntity() },
            castLinks = cast.map { it.toMovieCastLink(imdbId) },
        )
    }

    override fun observeGenres(): Flow<List<Genre>> =
        dao.observeGenres()
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun refreshGenres() {
        val genres = moviesApi.getGenres()
        dao.upsertGenres(genres.map { it.toGenreEntity() })
    }

    override fun observeFavorites(): Flow<List<Movie>> =
        dao.observeFavoriteMovies()
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeFavoriteCount(): Flow<Int> = dao.observeFavoriteCount()

    override suspend fun syncFavorites() = Unit

    override suspend fun addFavorite(imdbId: String) = Unit

    override suspend fun removeFavorite(imdbId: String) = Unit

    // --- Watchlist (requires auth — stubbed until Faza 2) ---

    override fun observeWatchlist(): Flow<List<Movie>> =
        dao.observeWatchlistMovies()
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeWatchlistCount(): Flow<Int> = dao.observeWatchlistCount()

    override suspend fun syncWatchlist() = Unit

    override suspend fun addToWatchlist(imdbId: String) = Unit

    override suspend fun removeFromWatchlist(imdbId: String) = Unit
}
