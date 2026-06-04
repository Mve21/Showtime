package rs.edu.raf.rma.auth.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignupScreen(
    onNavigateToMovies: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignupViewModel,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                SignupContract.SideEffect.NavigateToMovies -> onNavigateToMovies()
            }
        }
    }

    SignupContent(
        state = state,
        onNavigateToLogin = onNavigateToLogin,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun SignupContent(
    state: SignupContract.UiState,
    onNavigateToLogin: () -> Unit,
    eventPublisher: (SignupContract.UiEvent) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Registracija",
                    style = MaterialTheme.typography.displaySmall,
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = { eventPublisher(SignupContract.UiEvent.FullNameChanged(it)) },
                    label = { Text("Ime i prezime") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.error != null,
                    enabled = !state.isLoading,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.username,
                    onValueChange = { eventPublisher(SignupContract.UiEvent.UsernameChanged(it)) },
                    label = { Text("Korisničko ime") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.error != null,
                    enabled = !state.isLoading,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { eventPublisher(SignupContract.UiEvent.PasswordChanged(it)) },
                    label = { Text("Lozinka") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { eventPublisher(SignupContract.UiEvent.SignupClicked) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.error != null,
                    enabled = !state.isLoading,
                )

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { eventPublisher(SignupContract.UiEvent.SignupClicked) },
                    enabled = !state.isLoading &&
                        state.fullName.isNotBlank() &&
                        state.username.isNotBlank() &&
                        state.password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Registruj se")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text("Već imaš nalog? Prijavi se")
                }
            }
        }
    }
}
