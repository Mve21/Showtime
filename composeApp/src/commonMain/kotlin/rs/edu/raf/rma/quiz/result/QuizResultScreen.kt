package rs.edu.raf.rma.quiz.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rs.edu.raf.rma.core.format1d
import rs.edu.raf.rma.quiz.db.QuizSessionEntity

@Composable
fun QuizResultScreen(
    onNavigateToSession: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: QuizResultViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                QuizResultContract.SideEffect.NavigateToSession -> onNavigateToSession()
                QuizResultContract.SideEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    QuizResultContent(
        state = state,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun QuizResultContent(
    state: QuizResultContract.UiState,
    eventPublisher: (QuizResultContract.UiEvent) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val session = state.session
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Rezultati",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (session != null) {
                    ScoreDisplay(score = session.score)
                    Spacer(modifier = Modifier.height(32.dp))
                    StatsRow(session = session)
                } else {
                    Text("Nema podataka o sesiji", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { eventPublisher(QuizResultContract.UiEvent.PlayAgain) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Igraj ponovo")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { eventPublisher(QuizResultContract.UiEvent.GoHome) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Nazad na početak")
                }
            }
        }
    }
}

@Composable
private fun ScoreDisplay(score: Float) {
    val scoreColor = when {
        score >= 80 -> Color(0xFF4CAF50)
        score >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = score.format1d(),
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = scoreColor,
        )
        Text(
            text = "/ 100",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatsRow(session: QuizSessionEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(label = "Tačnih", value = session.correctCount.toString(), color = Color(0xFF4CAF50))
        StatItem(label = "Pogrešnih", value = session.incorrectCount.toString(), color = Color(0xFFF44336))
        StatItem(label = "Vreme", value = "${session.timeUsedSecs}s", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.size(width = 96.dp, height = 80.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}
