package rs.edu.raf.rma.quiz.session

import rs.edu.raf.rma.ui.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.Flow
import rs.edu.raf.rma.quiz.domain.QuizQuestion

private val CorrectColor = Color(0xFF4CAF50)
private val WrongColor = Color(0xFFF44336)

@Composable
fun QuizScreen(
    state: QuizContract.UiState,
    onEvent: (QuizContract.UiEvent) -> Unit,
    sideEffect: Flow<QuizContract.SideEffect>,
    onNavigateToResult: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    BackHandler { onEvent(QuizContract.UiEvent.BackPressed) }

    LaunchedEffect(Unit) {
        sideEffect.collect { effect ->
            when (effect) {
                QuizContract.SideEffect.NavigateToResult -> onNavigateToResult()
                QuizContract.SideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    if (state.showAbandonDialog) {
        AbandonDialog(
            onConfirm = { onEvent(QuizContract.UiEvent.AbandonConfirmed) },
            onDismiss = { onEvent(QuizContract.UiEvent.AbandonDismissed) },
        )
    }

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error, textAlign = TextAlign.Center)
            }
        }
        state.currentQuestion != null -> {
            QuizContent(state = state, onEvent = onEvent)
        }
    }
}

@Composable
private fun QuizContent(
    state: QuizContract.UiState,
    onEvent: (QuizContract.UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TimerSection(
            timeRemaining = state.timeRemainingSeconds,
            currentQuestion = state.currentIndex + 1,
            totalQuestions = state.totalQuestions,
        )

        AnimatedContent(
            targetState = state.currentIndex,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            label = "question_transition",
        ) { index ->
            val question = state.questions.getOrNull(index) ?: return@AnimatedContent
            QuestionSection(question = question)
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnswerButtons(
            question = state.currentQuestion!!,
            selectedOptionIndex = state.selectedOptionIndex,
            isFeedbackPhase = state.isFeedbackPhase,
            onAnswerClick = { onEvent(QuizContract.UiEvent.AnswerSelected(it)) },
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TimerSection(timeRemaining: Int, currentQuestion: Int, totalQuestions: Int) {
    val progress by animateFloatAsState(
        targetValue = timeRemaining / 60f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timer_progress",
    )
    val timerColor = when {
        timeRemaining > 30 -> CorrectColor
        timeRemaining > 10 -> Color(0xFFFF9800)
        else -> WrongColor
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "Pitanje $currentQuestion / $totalQuestions  ·  ${timeRemaining}s",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = timerColor,
        )
    }
}

@Composable
private fun QuestionSection(question: QuizQuestion) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = when (question) {
                is QuizQuestion.GuessMovie -> question.imageUrl
                is QuizQuestion.GuessYear -> question.posterUrl
                is QuizQuestion.GuessLeadActor -> question.posterUrl
            },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )

        if (question is QuizQuestion.GuessYear || question is QuizQuestion.GuessLeadActor) {
            val title = when (question) {
                is QuizQuestion.GuessYear -> question.title
                is QuizQuestion.GuessLeadActor -> question.title
                else -> ""
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        val questionLabel = when (question) {
            is QuizQuestion.GuessMovie -> "Koji je ovo film?"
            is QuizQuestion.GuessYear -> "Koje godine je ovaj film izašao?"
            is QuizQuestion.GuessLeadActor -> "Ko glumi u ovom filmu?"
        }
        Text(
            text = questionLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun AnswerButtons(
    question: QuizQuestion,
    selectedOptionIndex: Int?,
    isFeedbackPhase: Boolean,
    onAnswerClick: (Int) -> Unit,
) {
    val options: List<String> = when (question) {
        is QuizQuestion.GuessMovie -> question.options
        is QuizQuestion.GuessYear -> question.options.map { it.toString() }
        is QuizQuestion.GuessLeadActor -> question.options
    }
    val correctIndex = question.correctOptionIndex

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, option ->
            val containerColor = when {
                !isFeedbackPhase -> MaterialTheme.colorScheme.secondaryContainer
                index == correctIndex -> CorrectColor
                index == selectedOptionIndex -> WrongColor
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
            val contentColor = when {
                !isFeedbackPhase -> MaterialTheme.colorScheme.onSecondaryContainer
                index == correctIndex || index == selectedOptionIndex -> Color.White
                else -> MaterialTheme.colorScheme.onSecondaryContainer
            }

            Button(
                onClick = { onAnswerClick(index) },
                enabled = !isFeedbackPhase,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = containerColor,
                    disabledContentColor = contentColor,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = option,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun AbandonDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Napusti kviz?") },
        text = { Text("Napuštanjem kviza izgubićeš trenutni napredak.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Napusti", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Nastavi") }
        },
    )
}
