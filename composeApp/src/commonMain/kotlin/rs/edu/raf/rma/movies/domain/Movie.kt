package rs.edu.raf.rma.movies.domain

data class Movie(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val posterPath: String?,
    val genres: List<Genre>,
)
