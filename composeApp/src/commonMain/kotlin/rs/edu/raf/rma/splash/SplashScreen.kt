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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen(
    onNavigateToMovies: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: SplashViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.bootState) {
        if (state.bootState is BootState.Success) {
            if (state.isLoggedIn) onNavigateToMovies() else onNavigateToAuth()
        }
    }

    SplashContent(
        state = state,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun SplashContent(
    state: SplashContract.UiState,
    eventPublisher: (SplashContract.UiEvent) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val bootState = state.bootState) {
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
                        Button(onClick = { eventPublisher(SplashContract.UiEvent.Retry) }) {
                            Text("Pokušaj ponovo")
                        }
                    }
                }
            }
        }
    }
}
