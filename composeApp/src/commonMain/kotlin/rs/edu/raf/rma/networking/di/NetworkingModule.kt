package rs.edu.raf.rma.networking.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.dsl.module
import rs.edu.raf.rma.networking.HttpClientFactory
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.networking.createMoviesApi

val networkingModule = module {

    single<HttpClient> {
        HttpClientFactory.createHttpClientWithDefaultConfig()
    }

    single<MoviesApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>())
            .baseUrl("https://rma.finlab.rs/")
            .build()
            .createMoviesApi()
    }
}
