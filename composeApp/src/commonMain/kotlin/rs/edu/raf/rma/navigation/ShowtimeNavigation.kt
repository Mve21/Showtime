package rs.edu.raf.rma.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.auth.login.LoginScreen
import rs.edu.raf.rma.auth.login.LoginViewModel
import rs.edu.raf.rma.auth.signup.SignupScreen
import rs.edu.raf.rma.auth.signup.SignupViewModel
import rs.edu.raf.rma.movies.details.MovieDetailScreen
import rs.edu.raf.rma.movies.favorites.FavoritesScreen
import rs.edu.raf.rma.movies.favorites.FavoritesViewModel
import rs.edu.raf.rma.movies.watchlist.WatchlistScreen
import rs.edu.raf.rma.movies.watchlist.WatchlistViewModel
import rs.edu.raf.rma.profile.ProfileScreen
import rs.edu.raf.rma.profile.ProfileViewModel
import rs.edu.raf.rma.quiz.landing.QuizLandingScreen
import rs.edu.raf.rma.quiz.landing.QuizLandingViewModel
import rs.edu.raf.rma.quiz.result.QuizResultScreen
import rs.edu.raf.rma.quiz.result.QuizResultViewModel
import rs.edu.raf.rma.quiz.session.QuizScreen
import rs.edu.raf.rma.quiz.session.QuizViewModel
import rs.edu.raf.rma.movies.list.MoviesListScreen
import rs.edu.raf.rma.splash.SplashScreen
import rs.edu.raf.rma.splash.SplashViewModel

private val bottomBarRoutes = setOf("movies", "favorites", "watchlist", "profile", "quiz_landing")

@Composable
fun ShowtimeNavigation() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "movies",
                        onClick = {
                            navController.navigate("movies") {
                                popUpTo("movies") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Filmovi") },
                        label = { Text("Filmovi") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "favorites",
                        onClick = {
                            navController.navigate("favorites") {
                                popUpTo("movies") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = "Omiljeni") },
                        label = { Text("Omiljeni") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "watchlist",
                        onClick = {
                            navController.navigate("watchlist") {
                                popUpTo("movies") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Bookmark, contentDescription = "Watchlist") },
                        label = { Text("Watchlist") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "quiz_landing",
                        onClick = {
                            navController.navigate("quiz_landing") {
                                popUpTo("movies") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Quiz, contentDescription = "Kviz") },
                        label = { Text("Kviz") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo("movies") { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Profil") },
                        label = { Text("Profil") },
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues),
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
                    onNavigateToLogin = { navController.navigateUp() },
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
                arguments = listOf(navArgument("imdbId") { type = NavType.StringType }),
            ) {
                MovieDetailScreen(
                    onBackClick = { navController.navigateUp() },
                )
            }

            composable(route = "favorites") {
                val viewModel = koinViewModel<FavoritesViewModel>()
                val state by viewModel.state.collectAsState()
                FavoritesScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    sideEffect = viewModel.sideEffect,
                    onNavigateToDetail = { imdbId ->
                        navController.navigate("movies/$imdbId")
                    },
                )
            }

            composable(route = "watchlist") {
                val viewModel = koinViewModel<WatchlistViewModel>()
                val state by viewModel.state.collectAsState()
                WatchlistScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    sideEffect = viewModel.sideEffect,
                    onNavigateToDetail = { imdbId ->
                        navController.navigate("movies/$imdbId")
                    },
                )
            }

            composable(route = "profile") {
                val viewModel = koinViewModel<ProfileViewModel>()
                val state by viewModel.state.collectAsState()
                ProfileScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    sideEffect = viewModel.sideEffect,
                    onNavigateToAuth = {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable(route = "quiz_landing") {
                val viewModel = koinViewModel<QuizLandingViewModel>()
                val state by viewModel.state.collectAsState()
                QuizLandingScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    sideEffect = viewModel.sideEffect,
                    onNavigateToSession = {
                        navController.navigate("quiz_session")
                    },
                )
            }

            composable(route = "quiz_session") {
                val viewModel = koinViewModel<QuizViewModel>()
                val state by viewModel.state.collectAsState()
                QuizScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    sideEffect = viewModel.sideEffect,
                    onNavigateToResult = {
                        navController.navigate("quiz_result") {
                            popUpTo("quiz_session") { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                )
            }

            composable(route = "quiz_result") {
                val viewModel = koinViewModel<QuizResultViewModel>()
                val state by viewModel.state.collectAsState()
                QuizResultScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    sideEffect = viewModel.sideEffect,
                    onNavigateToSession = {
                        navController.navigate("quiz_session") {
                            popUpTo("quiz_result") { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate("quiz_landing") {
                            popUpTo("quiz_result") { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
