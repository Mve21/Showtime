package rs.edu.raf.rma.movies.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey val imdbId: String,
    val tmdbId: String?,
    val name: String,
    val profilePath: String?,
    val department: String?,
    val professions: String?,
    val birthYear: Int?,
    val deathYear: Int?,
    val popularity: Float?,
    val gender: Int?,
)
