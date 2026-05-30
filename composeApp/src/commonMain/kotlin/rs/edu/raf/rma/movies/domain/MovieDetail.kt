package rs.edu.raf.rma.movies.domain

data class MovieDetail(
    val movie: Movie,
    val overview: String?,
    val tagline: String?,
    val releaseDate: String?,
    val runtime: Int?,
    val imdbVotes: Int?,
    val tmdbRating: Float?,
    val tmdbVotes: Int?,
    val backdropPath: String?,
    val originalTitle: String?,
    val languageCode: String?,
    val cast: List<Person>,
    val isFavorite: Boolean,
    val isInWatchlist: Boolean,
)
