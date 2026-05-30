package rs.edu.raf.rma.movies.data

import rs.edu.raf.rma.movies.db.FavoriteEntity
import rs.edu.raf.rma.movies.db.GenreEntity
import rs.edu.raf.rma.movies.db.MovieCastCrossRef
import rs.edu.raf.rma.movies.db.MovieDetailEntity
import rs.edu.raf.rma.movies.db.MovieDetailFull
import rs.edu.raf.rma.movies.db.MovieEntity
import rs.edu.raf.rma.movies.db.MovieGenreCrossRef
import rs.edu.raf.rma.movies.db.MovieWithGenres
import rs.edu.raf.rma.movies.db.PersonEntity
import rs.edu.raf.rma.movies.db.WatchlistEntity
import rs.edu.raf.rma.movies.domain.Genre
import rs.edu.raf.rma.movies.domain.Movie
import rs.edu.raf.rma.movies.domain.MovieDetail
import rs.edu.raf.rma.movies.domain.Person
import rs.edu.raf.rma.networking.model.GenreApiModel
import rs.edu.raf.rma.networking.model.MovieApiModel
import rs.edu.raf.rma.networking.model.MovieListItemApiModel
import rs.edu.raf.rma.networking.model.PersonSummaryApiModel
import kotlin.time.Clock


fun MovieListItemApiModel.toMovieEntity(): MovieEntity = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
)

fun MovieListItemApiModel.toGenreEntities(): List<GenreEntity> =
    genres.map { it.toGenreEntity() }

fun MovieListItemApiModel.toMovieGenreLinks(): List<MovieGenreCrossRef> =
    genres.map { MovieGenreCrossRef(movieId = imdbId, genreId = it.id) }

fun MovieListItemApiModel.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    movieId = imdbId,
    addedAt = Clock.System.currentTimeMillis(),
)

fun MovieListItemApiModel.toWatchlistEntity(): WatchlistEntity = WatchlistEntity(
    movieId = imdbId,
    addedAt = Clock.System.currentTimeMillis(),
)

fun MovieApiModel.toMovieEntity(): MovieEntity = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
)

fun MovieApiModel.toMovieDetailEntity(): MovieDetailEntity = MovieDetailEntity(
    imdbId = imdbId,
    tmdbId = tmdbId,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    releaseDate = releaseDate,
    runtime = runtime,
    budget = budget,
    revenue = revenue,
    languageCode = languageCode,
    popularity = popularity,
    tmdbRating = tmdbRating,
    tmdbVotes = tmdbVotes,
    backdropPath = backdropPath,
    homepage = homepage,
)

fun MovieApiModel.toGenreEntities(): List<GenreEntity> =
    genres.map { it.toGenreEntity() }

fun MovieApiModel.toMovieGenreLinks(): List<MovieGenreCrossRef> =
    genres.map { MovieGenreCrossRef(movieId = imdbId, genreId = it.id) }

fun GenreApiModel.toGenreEntity(): GenreEntity = GenreEntity(
    id = id,
    name = name,
)

fun PersonSummaryApiModel.toPersonEntity(): PersonEntity = PersonEntity(
    imdbId = imdbId,
    tmdbId = null,
    name = name,
    profilePath = profilePath,
    department = department,
    professions = professions,
    birthYear = null,
    deathYear = null,
    popularity = null,
    gender = null,
)

fun PersonSummaryApiModel.toMovieCastLink(movieId: String): MovieCastCrossRef =
    MovieCastCrossRef(
        movieId = movieId,
        personId = imdbId,
        department = department,
        professions = professions,
    )


fun GenreEntity.toDomain(): Genre = Genre(
    id = id,
    name = name,
)

fun PersonEntity.toDomain(): Person = Person(
    imdbId = imdbId,
    name = name,
    profilePath = profilePath,
    department = department,
    professions = professions,
)

fun MovieWithGenres.toDomain(): Movie = Movie(
    imdbId = movie.imdbId,
    title = movie.title,
    year = movie.year,
    imdbRating = movie.imdbRating,
    posterPath = movie.posterPath,
    genres = genres.map { it.toDomain() },
)

fun MovieDetailFull.toDomain(isFavorite: Boolean, isInWatchlist: Boolean): MovieDetail =
    MovieDetail(
        movie = Movie(
            imdbId = movie.imdbId,
            title = movie.title,
            year = movie.year,
            imdbRating = movie.imdbRating,
            posterPath = movie.posterPath,
            genres = genres.map { it.toDomain() },
        ),
        overview = details?.overview,
        tagline = details?.tagline,
        releaseDate = details?.releaseDate,
        runtime = details?.runtime,
        imdbVotes = movie.imdbVotes,
        tmdbRating = details?.tmdbRating,
        tmdbVotes = details?.tmdbVotes,
        backdropPath = details?.backdropPath,
        originalTitle = details?.originalTitle,
        languageCode = details?.languageCode,
        cast = cast.map { it.toDomain() },
        isFavorite = isFavorite,
        isInWatchlist = isInWatchlist,
    )
