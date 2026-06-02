package rs.edu.raf.rma.movies.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.edu.raf.rma.movies.data.MovieRepositoryImpl
import rs.edu.raf.rma.movies.details.MovieDetailViewModel
import rs.edu.raf.rma.movies.domain.MovieRepository
import rs.edu.raf.rma.movies.favorites.FavoritesViewModel
import rs.edu.raf.rma.movies.list.MoviesListViewModel

val moviesModule = module {
    single { MovieRepositoryImpl(appDatabase = get(), moviesApi = get()) } bind MovieRepository::class
    viewModelOf(::MoviesListViewModel)
    viewModelOf(::MovieDetailViewModel)
    viewModelOf(::FavoritesViewModel)
}
