package rs.edu.raf.rma.movies.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Transaction
    @Query("""
        SELECT DISTINCT movies.* FROM movies
        LEFT JOIN movie_genres ON movies.imdbId = movie_genres.movieId
        WHERE (:genreId IS NULL OR movie_genres.genreId = :genreId)
        AND (:minYear IS NULL OR movies.year >= :minYear)
        AND (:maxYear IS NULL OR movies.year <= :maxYear)
        AND (:minRating IS NULL OR movies.imdbRating >= :minRating)
        AND (:query IS NULL OR LOWER(movies.title) LIKE '%' || LOWER(:query) || '%')
        ORDER BY movies.title ASC
    """)
    fun observeMovies(
        genreId: Int?,
        minYear: Int?,
        maxYear: Int?,
        minRating: Float?,
        query: String?,
    ): Flow<List<MovieWithGenres>>

    @Transaction
    @Query("SELECT * FROM movies WHERE imdbId = :imdbId")
    fun observeMovieDetail(imdbId: String): Flow<MovieDetailFull?>

    @Transaction
    @Query("""
        SELECT movies.* FROM movies
        INNER JOIN favorites ON movies.imdbId = favorites.movieId
        ORDER BY favorites.addedAt DESC
    """)
    fun observeFavoriteMovies(): Flow<List<MovieWithGenres>>

    @Transaction
    @Query("""
        SELECT movies.* FROM movies
        INNER JOIN watchlist ON movies.imdbId = watchlist.movieId
        ORDER BY watchlist.addedAt DESC
    """)
    fun observeWatchlistMovies(): Flow<List<MovieWithGenres>>

    @Query("SELECT COUNT(*) > 0 FROM favorites WHERE movieId = :movieId")
    fun observeIsFavorite(movieId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) > 0 FROM watchlist WHERE movieId = :movieId")
    fun observeIsInWatchlist(movieId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM watchlist")
    fun observeWatchlistCount(): Flow<Int>


    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun observeGenres(): Flow<List<GenreEntity>>

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Upsert
    suspend fun upsertMovieDetail(detail: MovieDetailEntity)

    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Upsert
    suspend fun upsertPeople(people: List<PersonEntity>)

    @Query("DELETE FROM movie_genres WHERE movieId = :movieId")
    suspend fun deleteMovieGenreLinks(movieId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovieGenreLinks(links: List<MovieGenreCrossRef>)

    @Transaction
    suspend fun replaceMovieGenreLinks(movieId: String, genreIds: List<Int>) {
        deleteMovieGenreLinks(movieId)
        insertMovieGenreLinks(genreIds.map { MovieGenreCrossRef(movieId, it) })
    }

    @Query("DELETE FROM movie_cast WHERE movieId = :movieId")
    suspend fun deleteMovieCastLinks(movieId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovieCastLinks(links: List<MovieCastCrossRef>)

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE movieId = :movieId")
    suspend fun deleteFavorite(movieId: String)

    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()

    @Upsert
    suspend fun upsertWatchlistItem(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE movieId = :movieId")
    suspend fun deleteWatchlistItem(movieId: String)

    @Query("DELETE FROM watchlist")
    suspend fun deleteAllWatchlist()

    @Transaction
    suspend fun refreshMovieListTransaction(movies: List<MovieEntity>) {
        upsertMovies(movies)
    }

    @Transaction
    suspend fun refreshMovieDetailTransaction(
        movie: MovieEntity,
        detail: MovieDetailEntity,
        genres: List<GenreEntity>,
        genreIds: List<Int>,
        cast: List<PersonEntity>,
        castLinks: List<MovieCastCrossRef>,
    ) {
        upsertMovies(listOf(movie))
        upsertMovieDetail(detail)
        upsertGenres(genres)
        replaceMovieGenreLinks(movie.imdbId, genreIds)
        upsertPeople(cast)
        deleteMovieCastLinks(movie.imdbId)
        insertMovieCastLinks(castLinks)
    }
}
