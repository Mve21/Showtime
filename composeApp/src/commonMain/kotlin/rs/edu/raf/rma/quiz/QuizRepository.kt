package rs.edu.raf.rma.quiz

import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    fun observeBestScore(): Flow<Float?>
    fun observeTotalPlays(): Flow<Int>
}
