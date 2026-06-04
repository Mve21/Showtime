package rs.edu.raf.rma.movies.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import rs.edu.raf.rma.movies.db.FavoriteEntity
import rs.edu.raf.rma.movies.db.WatchlistEntity
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.movies.domain.Genre
import rs.edu.raf.rma.movies.domain.Movie
import rs.edu.raf.rma.movies.domain.MovieDetail
import rs.edu.raf.rma.movies.domain.MovieRepository
import rs.edu.raf.rma.networking.MoviesApi

class MovieRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val moviesApi: MoviesApi,
) : MovieRepository {

    private val dao = appDatabase.movieDao()
    private var favoritesSynced = false
    private var watchlistSynced = false

    override fun observeMovies(
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Float?,
        query: String?,
        sortBy: String?,
        sortOrder: String?,
    ): Flow<List<Movie>> =
        dao.observeMovies(genreId, minYear, maxYear, minRating, query)
            .distinctUntilChanged()
            .map { rows ->
                val movies = rows.map { it.toDomain() }
                val ascending = sortOrder == "asc"
                when (sortBy) {
                    "year"       -> if (ascending) movies.sortedBy { it.year }
                                    else movies.sortedByDescending { it.year }
                    "title"      -> if (ascending) movies.sortedBy { it.title }
                                    else movies.sortedByDescending { it.title }
                    "imdbRating" -> if (ascending) movies.sortedBy { it.imdbRating }
                                    else movies.sortedByDescending { it.imdbRating }
                    else         -> movies.sortedByDescending { it.imdbRating }
                }
            }

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

    override suspend fun fetchMovieDetail(imdbId: String) {
        val movie = moviesApi.getMovie(imdbId)
        val cast = moviesApi.getMovieCast(imdbId).items
        dao.refreshMovieDetailTransaction(
            movie = movie.toMovieEntity(),
            detail = movie.toMovieDetailEntity(),
            genres = movie.toGenreEntities(),
            genreIds = movie.genres.map { it.id },
            cast = cast.map { it.toPersonEntity() },
            castLinks = cast.mapIndexed { index, person -> person.toMovieCastLink(imdbId, index) },
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

    override suspend fun syncFavorites(force: Boolean) {
        if (!force && favoritesSynced) return
        val serverFavorites = moviesApi.getFavorites()
        dao.upsertMovies(serverFavorites.map { it.toMovieEntity() })
        dao.upsertGenres(serverFavorites.flatMap { it.toGenreEntities() }.distinctBy { it.id })
        serverFavorites.forEach { item ->
            dao.replaceMovieGenreLinks(item.imdbId, item.genres.map { it.id })
        }
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        dao.replaceFavorites(serverFavorites.map { FavoriteEntity(movieId = it.imdbId, addedAt = now) })
        favoritesSynced = true
    }

    override suspend fun addFavorite(imdbId: String) {
        dao.upsertFavorite(FavoriteEntity(movieId = imdbId, addedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()))
        try {
            moviesApi.addFavorite(imdbId)
        } catch (e: Exception) {
            dao.deleteFavorite(imdbId)
            throw e
        }
    }

    override suspend fun removeFavorite(imdbId: String) {
        dao.deleteFavorite(imdbId)
        try {
            moviesApi.removeFavorite(imdbId)
        } catch (e: Exception) {
            dao.upsertFavorite(FavoriteEntity(movieId = imdbId, addedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()))
            throw e
        }
    }

    override fun observeWatchlist(): Flow<List<Movie>> =
        dao.observeWatchlistMovies()
            .distinctUntilChanged()
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeWatchlistCount(): Flow<Int> = dao.observeWatchlistCount()

    override suspend fun syncWatchlist(force: Boolean) {
        if (!force && watchlistSynced) return
        val serverWatchlist = moviesApi.getWatchlist()
        dao.upsertMovies(serverWatchlist.map { it.toMovieEntity() })
        dao.upsertGenres(serverWatchlist.flatMap { it.toGenreEntities() }.distinctBy { it.id })
        serverWatchlist.forEach { item ->
            dao.replaceMovieGenreLinks(item.imdbId, item.genres.map { it.id })
        }
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        dao.replaceWatchlist(serverWatchlist.map { WatchlistEntity(movieId = it.imdbId, addedAt = now) })
        watchlistSynced = true
    }

    override suspend fun addToWatchlist(imdbId: String) {
        dao.upsertWatchlistItem(WatchlistEntity(movieId = imdbId, addedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()))
        try {
            moviesApi.addToWatchlist(imdbId)
        } catch (e: Exception) {
            dao.deleteWatchlistItem(imdbId)
            throw e
        }
    }

    override suspend fun removeFromWatchlist(imdbId: String) {
        dao.deleteWatchlistItem(imdbId)
        try {
            moviesApi.removeFromWatchlist(imdbId)
        } catch (e: Exception) {
            dao.upsertWatchlistItem(WatchlistEntity(movieId = imdbId, addedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()))
            throw e
        }
    }

    override suspend fun bootstrapCatalog() {
        if (dao.countMovies() >= 200) return

        // Dohvatamo 2 stranice po 100 filmova paralelno
        val (page1, page2) = coroutineScope {
            val d1 = async {
                moviesApi.getMovies(page = 1, pageSize = 100, sortBy = "imdb_votes", sortOrder = "desc")
            }
            val d2 = async {
                moviesApi.getMovies(page = 2, pageSize = 100, sortBy = "imdb_votes", sortOrder = "desc")
            }
            d1.await() to d2.await()
        }

        val allItems = page1.items + page2.items
        dao.upsertGenres(allItems.flatMap { it.toGenreEntities() }.distinctBy { it.id })
        dao.upsertMovies(allItems.map { it.toMovieEntity() })
        allItems.forEach { item ->
            dao.replaceMovieGenreLinks(item.imdbId, item.genres.map { it.id })
        }

        // Dohvatamo detalje (cast, backdrop...) za prvih 100 paralelno
        coroutineScope {
            page1.items.forEach { item ->
                launch {
                    runCatching { fetchMovieDetail(item.imdbId) }
                }
            }
        }
    }
}
