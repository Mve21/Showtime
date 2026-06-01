package rs.edu.raf.rma.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(
    bootState: BootState,
    isLoggedIn: Boolean,
    onNavigateToMovies: () -> Unit,
    onNavigateToAuthLanding: () -> Unit,
    onRetry: () -> Unit,
) {
    LaunchedEffect(bootState) {
        if (bootState is BootState.Success) {
            if (isLoggedIn) onNavigateToMovies() else onNavigateToAuthLanding()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (bootState) {
                BootState.Loading, BootState.Success -> CircularProgressIndicator()
                is BootState.Failed -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Greška pri pokretanju",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = bootState.error.message ?: "Nepoznata greška",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("Pokušaj ponovo")
                        }
                    }
                }
            }
        }
    }
}
