package rs.edu.raf.rma.movies.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = MovieEntity::class,
            parentColumns = ["imdbId"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FavoriteEntity(
    @PrimaryKey val movieId: String,
    val addedAt: Long,
)
