package rs.edu.raf.rma.quiz.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.edu.raf.rma.quiz.QuizRepository
import rs.edu.raf.rma.quiz.QuizRepositoryImpl
import rs.edu.raf.rma.quiz.landing.QuizLandingViewModel
import rs.edu.raf.rma.quiz.result.QuizResultViewModel
import rs.edu.raf.rma.quiz.session.QuizViewModel

val quizModule = module {
    single { QuizRepositoryImpl(appDatabase = get(), moviesApi = get()) } bind QuizRepository::class
    viewModelOf(::QuizLandingViewModel)
    viewModelOf(::QuizViewModel)
    viewModelOf(::QuizResultViewModel)
}
