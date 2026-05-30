package rs.edu.raf.rma.movies.domain

data class Person(
    val imdbId: String,
    val name: String,
    val profilePath: String?,
    val department: String?,
    val professions: String?,
)
