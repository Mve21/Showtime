package rs.edu.raf.rma.quiz.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_sessions")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Float,
    val correctCount: Int,
    val incorrectCount: Int,
    val timeUsedSecs: Long,
    val playedAt: Long,
)
