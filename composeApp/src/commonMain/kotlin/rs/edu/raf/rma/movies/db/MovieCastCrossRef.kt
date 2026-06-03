package rs.edu.raf.rma.movies.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_cast",
    primaryKeys = ["movieId", "personId"],
    indices = [Index("personId")],
)
data class MovieCastCrossRef(
    val movieId: String,
    val personId: String,
    val department: String?,
    val professions: String?,
    val castOrder: Int = 0,
)
