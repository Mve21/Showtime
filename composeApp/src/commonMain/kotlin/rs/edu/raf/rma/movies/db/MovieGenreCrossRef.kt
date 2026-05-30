package rs.edu.raf.rma.movies.db

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "movie_genres",
    primaryKeys = ["movieId", "genreId"],
    indices = [Index("genreId")],
)
data class MovieGenreCrossRef(
    val movieId: String,
    val genreId: Int,
)
