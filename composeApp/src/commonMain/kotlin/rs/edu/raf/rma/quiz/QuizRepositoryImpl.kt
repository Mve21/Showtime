package rs.edu.raf.rma.quiz

import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.core.db.AppDatabase

class QuizRepositoryImpl(
    private val appDatabase: AppDatabase,
) : QuizRepository {

    private val dao = appDatabase.quizDao()

    override fun observeBestScore(): Flow<Float?> = dao.observeBestScore()

    override fun observeTotalPlays(): Flow<Int> = dao.observeTotalPlays()
}
