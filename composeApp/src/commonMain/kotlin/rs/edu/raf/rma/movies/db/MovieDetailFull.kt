package rs.edu.raf.rma.movies.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MovieDetailFull(
    @Embedded val movie: MovieEntity,
    @Relation(parentColumn = "imdbId", entityColumn = "imdbId")
    val details: MovieDetailEntity?,
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
    @Relation(
        parentColumn = "imdbId",
        entityColumn = "imdbId",
        associateBy = Junction(
            value = MovieCastCrossRef::class,
            parentColumn = "movieId",
            entityColumn = "personId",
        ),
    )
    val cast: List<PersonEntity>,
)
