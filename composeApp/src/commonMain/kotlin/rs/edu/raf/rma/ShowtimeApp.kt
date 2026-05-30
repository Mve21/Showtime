package rs.edu.raf.rma

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import rs.edu.raf.rma.navigation.ShowtimeNavigation

@Composable
fun ShowtimeApp() {
    MaterialTheme {
        ShowtimeNavigation()
    }
}
