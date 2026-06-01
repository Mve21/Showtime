package rs.edu.raf.rma.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.auth.login.LoginScreen
import rs.edu.raf.rma.auth.login.LoginViewModel
import rs.edu.raf.rma.auth.signup.SignupScreen
import rs.edu.raf.rma.auth.signup.SignupViewModel
import rs.edu.raf.rma.movies.details.MovieDetailScreen
import rs.edu.raf.rma.movies.list.MoviesListScreen
import rs.edu.raf.rma.splash.SplashScreen
import rs.edu.raf.rma.splash.SplashViewModel

@Composable
fun ShowtimeNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash",
    ) {
        composable(route = "splash") {
            val viewModel = koinViewModel<SplashViewModel>()
            val bootState by viewModel.bootState.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            SplashScreen(
                bootState = bootState,
                isLoggedIn = isLoggedIn,
                onNavigateToMovies = {
                    navController.navigate("movies") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToAuthLanding = {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onRetry = viewModel::retryBoot,
            )
        }

        composable(route = "auth") {
            val viewModel = koinViewModel<LoginViewModel>()
            val state by viewModel.uiState.collectAsState()
            LoginScreen(
                state = state,
                onEvent = viewModel::onEvent,
                sideEffect = viewModel.sideEffect,
                onNavigateToMovies = {
                    navController.navigate("movies") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate("signup")
                },
            )
        }

        composable(route = "signup") {
            val viewModel = koinViewModel<SignupViewModel>()
            val state by viewModel.uiState.collectAsState()
            SignupScreen(
                state = state,
                onEvent = viewModel::onEvent,
                sideEffect = viewModel.sideEffect,
                onNavigateToMovies = {
                    navController.navigate("movies") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigateUp()
                },
            )
        }

        composable(route = "movies") {
            MoviesListScreen(
                onMovieClick = { imdbId ->
                    navController.navigate("movies/$imdbId")
                },
            )
        }

        composable(
            route = "movies/{imdbId}",
            arguments = listOf(
                navArgument("imdbId") { type = NavType.StringType },
            ),
        ) {
            MovieDetailScreen(
                onBackClick = { navController.navigateUp() },
            )
        }
    }
}
