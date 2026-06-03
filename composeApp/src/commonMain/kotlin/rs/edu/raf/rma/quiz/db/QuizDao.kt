package rs.edu.raf.rma.quiz.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Insert
    suspend fun insertSession(session: QuizSessionEntity)

    @Query("SELECT * FROM quiz_sessions ORDER BY playedAt DESC")
    fun observeAllSessions(): Flow<List<QuizSessionEntity>>

    @Query("SELECT MAX(score) FROM quiz_sessions")
    fun observeBestScore(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM quiz_sessions")
    fun observeTotalPlays(): Flow<Int>

    @Query("SELECT * FROM quiz_sessions ORDER BY playedAt DESC LIMIT 1")
    suspend fun getLastSession(): QuizSessionEntity?
}
