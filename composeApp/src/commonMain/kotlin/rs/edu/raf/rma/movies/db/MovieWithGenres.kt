package rs.edu.raf.rma.movies.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MovieWithGenres(
    @Embedded val movie: MovieEntity,
    @Relation(
        parentColumn = "imdbId",
        entityColumn = "id",
        associateBy = Junction(
            value = MovieGenreCrossRef::class,
            parentColumn = "movieId",
            entityColumn = "genreId",
        ),
    )
    val genres: List<GenreEntity>,
)
