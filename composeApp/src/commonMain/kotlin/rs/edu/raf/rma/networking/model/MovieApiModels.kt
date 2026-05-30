package rs.edu.raf.rma.networking.model

import kotlinx.serialization.Serializable

@Serializable
data class GenreApiModel(
    val id: Int,
    val name: String,
)

@Serializable
data class MovieListItemApiModel(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
)

@Serializable
data class CollectionSummaryApiModel(
    val id: Int,
    val name: String,
    val posterPath: String? = null,
    val backdropPath: String? = null,
)

@Serializable
data class MovieApiModel(
    val imdbId: String,
    val tmdbId: String? = null,
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val languageCode: String? = null,
    val popularity: Float? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val tmdbRating: Float? = null,
    val tmdbVotes: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val homepage: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
    val collection: CollectionSummaryApiModel? = null,
)

@Serializable
data class PersonSummaryApiModel(
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null,
)

@Serializable
data class PersonInfoApiModel(
    val imdbId: String,
    val tmdbId: String? = null,
    val name: String,
    val birthYear: Int? = null,
    val deathYear: Int? = null,
    val professions: String? = null,
    val department: String? = null,
    val popularity: Float? = null,
    val profilePath: String? = null,
    val gender: Int? = null,
)

@Serializable
data class PersonDetailApiModel(
    val person: PersonInfoApiModel,
    val movies: List<MovieListItemApiModel> = emptyList(),
)
