package rs.edu.raf.rma.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import rs.edu.raf.rma.movies.db.FavoriteEntity
import rs.edu.raf.rma.movies.db.GenreEntity
import rs.edu.raf.rma.movies.db.MovieCastCrossRef
import rs.edu.raf.rma.movies.db.MovieDao
import rs.edu.raf.rma.movies.db.MovieDetailEntity
import rs.edu.raf.rma.movies.db.MovieEntity
import rs.edu.raf.rma.movies.db.MovieGenreCrossRef
import rs.edu.raf.rma.movies.db.PersonEntity
import rs.edu.raf.rma.movies.db.WatchlistEntity
import rs.edu.raf.rma.quiz.db.QuizDao
import rs.edu.raf.rma.quiz.db.QuizSessionEntity

@Database(
    entities = [
        MovieEntity::class,
        MovieDetailEntity::class,
        GenreEntity::class,
        MovieGenreCrossRef::class,
        PersonEntity::class,
        MovieCastCrossRef::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        QuizSessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun quizDao(): QuizDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
