package rs.edu.raf.rma.quiz.di

import org.koin.dsl.bind
import org.koin.dsl.module
import rs.edu.raf.rma.quiz.QuizRepository
import rs.edu.raf.rma.quiz.QuizRepositoryImpl

val quizModule = module {
    single { QuizRepositoryImpl(appDatabase = get()) } bind QuizRepository::class
}
