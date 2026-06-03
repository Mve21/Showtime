package rs.edu.raf.rma.quiz.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizLandingScreen(
    state: QuizLandingContract.UiState,
    onEvent: (QuizLandingContract.UiEvent) -> Unit,
    sideEffect: Flow<QuizLandingContract.SideEffect>,
    onNavigateToSession: () -> Unit,
) {
    LaunchedEffect(Unit) {
        sideEffect.collect { effect ->
            when (effect) {
                QuizLandingContract.SideEffect.NavigateToSession -> onNavigateToSession()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kviz") }) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (state.phase) {
                QuizLandingContract.Phase.Checking,
                QuizLandingContract.Phase.Bootstrapping -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (state.phase == QuizLandingContract.Phase.Bootstrapping)
                            "Priprema filmova za kviz..." else "Proveravanje...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                QuizLandingContract.Phase.NotEnoughMovies -> {
                    Text(
                        text = "🎬",
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nema dovoljno filmova",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pretraži katalog i otvori detalje bar 2 filma da bi aktivirao sva tri tipa pitanja.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                QuizLandingContract.Phase.Ready -> {
                    Text(
                        text = "🎬",
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Showtime Kviz",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "10 pitanja · 60 sekundi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { onEvent(QuizLandingContract.UiEvent.StartClicked) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pokreni kviz")
                    }
                }
            }
        }
    }
}
