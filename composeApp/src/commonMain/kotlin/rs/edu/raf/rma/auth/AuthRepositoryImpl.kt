package rs.edu.raf.rma.auth

import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.networking.MoviesApi
import rs.edu.raf.rma.networking.model.LoginBody
import rs.edu.raf.rma.networking.model.SignupBody

class AuthRepositoryImpl(
    private val moviesApi: MoviesApi,
    private val authStore: AuthStore,
    private val appDatabase: AppDatabase,
) : AuthRepository {

    override suspend fun login(username: String, password: String) {
        val response = moviesApi.login(LoginBody(username = username, password = password))
        authStore.setAuthData(
            AuthData(
                accessToken = response.accessToken,
                userId = response.user.id,
                username = response.user.username,
                fullName = response.user.fullName,
            )
        )
    }

    override suspend fun signup(fullName: String, username: String, password: String) {
        val response = moviesApi.signup(
            SignupBody(username = username, password = password, fullName = fullName)
        )
        authStore.setAuthData(
            AuthData(
                accessToken = response.accessToken,
                userId = response.user.id,
                username = response.user.username,
                fullName = response.user.fullName,
            )
        )
    }

    override suspend fun logout() {
        appDatabase.movieDao().deleteAllFavorites()
        appDatabase.movieDao().deleteAllWatchlist()
        appDatabase.quizDao().deleteAllSessions()
        authStore.clearAuthData()
    }
}
