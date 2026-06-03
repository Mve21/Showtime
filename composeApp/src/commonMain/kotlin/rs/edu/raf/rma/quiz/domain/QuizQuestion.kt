package rs.edu.raf.rma.quiz.domain

sealed class QuizQuestion {
    abstract val correctOptionIndex: Int

    data class GuessMovie(
        val imageUrl: String,
        val options: List<String>,
        override val correctOptionIndex: Int,
    ) : QuizQuestion()

    data class GuessYear(
        val posterUrl: String,
        val title: String,
        val options: List<Int>,
        override val correctOptionIndex: Int,
    ) : QuizQuestion()

    data class GuessLeadActor(
        val posterUrl: String,
        val title: String,
        val options: List<String>,
        override val correctOptionIndex: Int,
    ) : QuizQuestion()
}
