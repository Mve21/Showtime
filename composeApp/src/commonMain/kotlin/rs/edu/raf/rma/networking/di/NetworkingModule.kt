package rs.edu.raf.rma.networking.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SetupRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.networking.HttpClientFactory
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.networking.createMoviesApi

val networkingModule = module {

    single<HttpClient>(Qualifiers.Unauthenticated) {
        HttpClientFactory.createHttpClientWithDefaultConfig()
    }

    single<HttpClient>(Qualifiers.Authenticated) {
        val authStoreLazy: Lazy<AuthStore> = inject()
        HttpClientFactory.createHttpClientWithDefaultConfig {
            installAuthPlugin(authStoreLazy)
        }
    }

    single<MoviesApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl("https://rma.finlab.rs/")
            .build()
            .createMoviesApi()
    }
}

private fun HttpClientConfig<*>.installAuthPlugin(authStoreLazy: Lazy<AuthStore>) =
    install(createClientPlugin("AuthPlugin") {

        on(SetupRequest) { request ->
            when (val authState = authStoreLazy.value.authState.value) {
                is AuthState.Authenticated -> {
                    request.header(
                        key = HttpHeaders.Authorization,
                        value = "Bearer ${authState.data.accessToken}",
                    )
                }
                AuthState.Unauthenticated -> Unit
            }
        }

        on(Send) { request ->
            val originalCall = proceed(request)
            if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                runBlocking { authStoreLazy.value.clearAuthData() }
            }
            originalCall
        }
    })
