package rs.edu.raf.rma.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import rs.edu.raf.rma.movies.details.MovieDetailScreen
import rs.edu.raf.rma.movies.list.MoviesListScreen

@Composable
fun ShowtimeNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "movies",
    ) {
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
