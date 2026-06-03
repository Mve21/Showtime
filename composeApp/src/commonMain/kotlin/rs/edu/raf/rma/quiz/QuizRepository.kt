package rs.edu.raf.rma.quiz

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.quiz.db.QuizSessionEntity
import rs.edu.raf.rma.quiz.domain.QuizQuestion

interface QuizRepository {
    fun observeBestScore(): Flow<Float?>
    fun observeTotalPlays(): Flow<Int>
    suspend fun hasEnoughMoviesForQuiz(): Boolean
    suspend fun bootstrapMovies()
    suspend fun generateSession(): List<QuizQuestion>
    suspend fun saveSession(session: QuizSessionEntity)
    suspend fun getLastSession(): QuizSessionEntity?
}
