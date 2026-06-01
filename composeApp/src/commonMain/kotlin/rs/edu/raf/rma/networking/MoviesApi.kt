package rs.edu.raf.rma.networking

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.rma.networking.model.AuthResponseApiModel
import rs.edu.raf.rma.networking.model.GenreApiModel
import rs.edu.raf.rma.networking.model.LeaderboardEntryApiModel
import rs.edu.raf.rma.networking.model.LoginBody
import rs.edu.raf.rma.networking.model.MovieApiModel
import rs.edu.raf.rma.networking.model.MovieIdBody
import rs.edu.raf.rma.networking.model.MovieListItemApiModel
import rs.edu.raf.rma.networking.model.PaginatedResponse
import rs.edu.raf.rma.networking.model.PersonDetailApiModel
import rs.edu.raf.rma.networking.model.PersonSummaryApiModel
import rs.edu.raf.rma.networking.model.PostQuizResultBody
import rs.edu.raf.rma.networking.model.PostQuizResultResponseApiModel
import rs.edu.raf.rma.networking.model.QuizResultApiModel
import rs.edu.raf.rma.networking.model.SignupBody
import rs.edu.raf.rma.networking.model.UserApiModel

interface MoviesApi {

    // --- Auth ---

    @POST("auth/signup")
    suspend fun signup(@Body body: SignupBody): AuthResponseApiModel

    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): AuthResponseApiModel

    // --- Catalog ---

    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("query") query: String? = null,
        @Query("genre_id") genreId: Int? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
    ): PaginatedResponse<MovieListItemApiModel>

    @GET("movies/{id}")
    suspend fun getMovie(@Path("id") id: String): MovieApiModel

    @GET("movies/{id}/cast")
    suspend fun getMovieCast(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
    ): PaginatedResponse<PersonSummaryApiModel>

    @GET("genres")
    suspend fun getGenres(): List<GenreApiModel>

    @GET("people/{id}")
    suspend fun getPerson(@Path("id") id: String): PersonDetailApiModel

    // --- User profile ---

    @GET("me")
    suspend fun getMe(): UserApiModel

    // --- Favorites ---

    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieListItemApiModel>

    @POST("me/favorites/{movieId}")
    suspend fun addFavorite(@Path("movieId") movieId: String)

    @DELETE("me/favorites/{movieId}")
    suspend fun removeFavorite(@Path("movieId") movieId: String)

    // --- Watchlist ---

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieListItemApiModel>

    @POST("me/watchlist/{movieId}")
    suspend fun addToWatchlist(@Path("movieId") movieId: String)

    @DELETE("me/watchlist/{movieId}")
    suspend fun removeFromWatchlist(@Path("movieId") movieId: String)

    // --- Leaderboard & Quiz results ---

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("category") category: Int? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): PaginatedResponse<LeaderboardEntryApiModel>

    @POST("leaderboard")
    suspend fun submitQuizResult(@Body body: PostQuizResultBody): PostQuizResultResponseApiModel

    @GET("me/quiz-results")
    suspend fun getMyQuizResults(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): PaginatedResponse<QuizResultApiModel>
}
