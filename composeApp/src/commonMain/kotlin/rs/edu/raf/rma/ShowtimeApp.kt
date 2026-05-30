package rs.edu.raf.rma

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import rs.edu.raf.rma.movies.list.MoviesListScreen

@Composable
fun ShowtimeApp() {
    MaterialTheme {
        MoviesListScreen()
    }
}
