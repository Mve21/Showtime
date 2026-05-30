package rs.edu.raf.rma.movies.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "movie_details",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["imdbId"],
            childColumns = ["imdbId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MovieDetailEntity(
    @PrimaryKey val imdbId: String,
    val tmdbId: String?,
    val originalTitle: String?,
    val overview: String?,
    val tagline: String?,
    val releaseDate: String?,
    val runtime: Int?,
    val budget: Long?,
    val revenue: Long?,
    val languageCode: String?,
    val popularity: Float?,
    val tmdbRating: Float?,
    val tmdbVotes: Int?,
    val backdropPath: String?,
    val homepage: String?,
)
