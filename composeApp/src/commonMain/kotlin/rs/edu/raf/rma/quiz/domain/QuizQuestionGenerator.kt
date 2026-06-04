package rs.edu.raf.rma.quiz.domain

import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.movies.db.MovieDetailEntity
import rs.edu.raf.rma.movies.db.MovieEntity
import rs.edu.raf.rma.movies.db.PersonEntity

private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500"
private const val QUESTIONS_PER_SESSION = 10
private const val MIN_PER_TYPE = 2
private const val MAX_PER_TYPE = 4

private enum class QuestionType { GuessMovie, GuessYear, GuessLeadActor }

class QuizQuestionGenerator(private val appDatabase: AppDatabase) {

    suspend fun generateSession(): List<QuizQuestion> {
        val dao = appDatabase.movieDao()

        val moviesWithImages = dao.getMoviesWithImages().shuffled()
        if (moviesWithImages.size < QUESTIONS_PER_SESSION) return emptyList()

        val allMovieDetails = dao.getAllMovieDetails().associateBy { it.imdbId }
        val moviesWithCast = dao.getMoviesWithCast().shuffled()
        val allPeople = dao.getAllPeople()

        val typeSequence = buildTypeSequence()

        val usedMovieIds = mutableSetOf<String>()
        val usedImageUrls = mutableSetOf<String>()
        val typeCounts = mutableMapOf(
            QuestionType.GuessMovie to 0,
            QuestionType.GuessYear to 0,
            QuestionType.GuessLeadActor to 0,
        )
        val questions = mutableListOf<QuizQuestion>()

        for (type in typeSequence) {
            if (questions.size >= QUESTIONS_PER_SESSION) break

            val question = when (type) {
                QuestionType.GuessMovie ->
                    generateGuessMovie(moviesWithImages, allMovieDetails, usedMovieIds, usedImageUrls)
                QuestionType.GuessYear ->
                    generateGuessYear(moviesWithImages, usedMovieIds, usedImageUrls)
                QuestionType.GuessLeadActor ->
                    generateGuessLeadActor(moviesWithCast, allPeople, usedMovieIds, usedImageUrls)
            } ?: continue

            questions.add(question)
            typeCounts[type] = typeCounts[type]!! + 1
        }

        return questions
    }

    private fun buildTypeSequence(): List<QuestionType> {
        val base = listOf(
            QuestionType.GuessMovie, QuestionType.GuessMovie,
            QuestionType.GuessYear, QuestionType.GuessYear,
            QuestionType.GuessLeadActor, QuestionType.GuessLeadActor,
        )
        val extra = (
            listOf(QuestionType.GuessMovie, QuestionType.GuessMovie) +
            listOf(QuestionType.GuessYear, QuestionType.GuessYear) +
            listOf(QuestionType.GuessLeadActor, QuestionType.GuessLeadActor)
        ).shuffled().take(4)

        return (base + extra).shuffled()
    }

    private fun generateGuessMovie(
        movies: List<MovieEntity>,
        details: Map<String, MovieDetailEntity>,
        usedMovieIds: MutableSet<String>,
        usedImageUrls: MutableSet<String>,
    ): QuizQuestion.GuessMovie? {
        val candidates = movies.filter { it.imdbId !in usedMovieIds }.shuffled()

        for (movie in candidates) {
            val imageUrl = resolveGuessMovieImage(movie, details[movie.imdbId]) ?: continue
            if (imageUrl in usedImageUrls) continue

            val wrongCandidates = movies.filter { it.imdbId !in usedMovieIds && it.imdbId != movie.imdbId }
            if (wrongCandidates.size < 3) continue

            val wrongMovies = wrongCandidates.shuffled().take(3)
            val wrongTitles = wrongMovies.map { it.title }

            val options = (listOf(movie.title) + wrongTitles).shuffled()
            val correctIndex = options.indexOf(movie.title)

            usedMovieIds.add(movie.imdbId)
            usedMovieIds.addAll(wrongMovies.map { it.imdbId })
            usedImageUrls.add(imageUrl)
            return QuizQuestion.GuessMovie(imageUrl, options, correctIndex)
        }
        return null
    }

    private fun generateGuessYear(
        movies: List<MovieEntity>,
        usedMovieIds: MutableSet<String>,
        usedImageUrls: MutableSet<String>,
    ): QuizQuestion.GuessYear? {
        val candidates = movies
            .filter { it.imdbId !in usedMovieIds && it.year != null && it.posterPath != null }
            .shuffled()

        for (movie in candidates) {
            val imageUrl = "$TMDB_IMAGE_BASE${movie.posterPath}"
            if (imageUrl in usedImageUrls) continue

            val wrongYears = generateWrongYears(movie.year!!)
            if (wrongYears.size < 3) continue

            val options = (listOf(movie.year) + wrongYears).shuffled()
            val correctIndex = options.indexOf(movie.year)

            usedMovieIds.add(movie.imdbId)
            usedImageUrls.add(imageUrl)
            return QuizQuestion.GuessYear(imageUrl, movie.title, options, correctIndex)
        }
        return null
    }

    private suspend fun generateGuessLeadActor(
        moviesWithCast: List<MovieEntity>,
        allPeople: List<PersonEntity>,
        usedMovieIds: MutableSet<String>,
        usedImageUrls: MutableSet<String>,
    ): QuizQuestion.GuessLeadActor? {
        val dao = appDatabase.movieDao()
        val candidates = moviesWithCast
            .filter { it.imdbId !in usedMovieIds && it.posterPath != null }
            .shuffled()

        for (movie in candidates) {
            val imageUrl = "$TMDB_IMAGE_BASE${movie.posterPath}"
            if (imageUrl in usedImageUrls) continue

            val cast = dao.getCastForMovie(movie.imdbId)
            if (cast.size < 3) continue

            val moviePersonIds = cast.map { it.imdbId }.toSet()
            val correct = cast.take(3).random()
            val wrong = allPeople
                .filter { it.imdbId !in moviePersonIds }
                .shuffled()
                .take(3)
            if (wrong.size < 3) continue

            val options = (listOf(correct.name) + wrong.map { it.name }).shuffled()
            val correctIndex = options.indexOf(correct.name)

            usedMovieIds.add(movie.imdbId)
            usedImageUrls.add(imageUrl)
            return QuizQuestion.GuessLeadActor(imageUrl, movie.title, options, correctIndex)
        }
        return null
    }

    private fun resolveGuessMovieImage(movie: MovieEntity, detail: MovieDetailEntity?): String? {
        val path = detail?.backdropPath ?: movie.posterPath ?: return null
        return "$TMDB_IMAGE_BASE$path"
    }

    private fun generateWrongYears(correct: Int): List<Int> {
        return ((-10..-1) + (1..10)).toList()
            .shuffled()
            .map { correct + it }
            .filter { it > 0 }
            .distinct()
            .take(3)
    }
}
