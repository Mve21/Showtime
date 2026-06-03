package rs.edu.raf.rma.quiz

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.movies.data.toGenreEntities
import rs.edu.raf.rma.movies.data.toMovieEntity
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.quiz.db.QuizSessionEntity
import rs.edu.raf.rma.quiz.domain.QuizQuestion
import rs.edu.raf.rma.quiz.domain.QuizQuestionGenerator

class QuizRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val moviesApi: MoviesApi,
) : QuizRepository {

    private val movieDao = appDatabase.movieDao()
    private val quizDao = appDatabase.quizDao()

    override fun observeBestScore(): Flow<Float?> = quizDao.observeBestScore()

    override fun observeTotalPlays(): Flow<Int> = quizDao.observeTotalPlays()

    override suspend fun hasEnoughMoviesForQuiz(): Boolean =
        movieDao.countMoviesWithImages() >= 10 && movieDao.countMoviesWithEnoughCast() >= 2

    override suspend fun bootstrapMovies() {
        val response = moviesApi.getMovies(
            page = 1,
            pageSize = 100,
            sortBy = "imdb_votes",
            sortOrder = "desc",
        )
        val items = response.items
        movieDao.upsertMovies(items.map { it.toMovieEntity() })
        movieDao.upsertGenres(items.flatMap { it.toGenreEntities() }.distinctBy { it.id })
        items.forEach { item ->
            movieDao.replaceMovieGenreLinks(item.imdbId, item.genres.map { it.id })
        }
    }

    override suspend fun generateSession(): List<QuizQuestion> =
        QuizQuestionGenerator(appDatabase).generateSession()

    override suspend fun saveSession(session: QuizSessionEntity) {
        quizDao.insertSession(session)
    }

    override suspend fun getLastSession(): QuizSessionEntity? =
        quizDao.getLastSession()
}
