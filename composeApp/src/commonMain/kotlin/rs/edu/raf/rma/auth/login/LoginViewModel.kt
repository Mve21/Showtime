package rs.edu.raf.rma.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rs.edu.raf.rma.auth.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginContract.UiState())
    val uiState: StateFlow<LoginContract.UiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<LoginContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onEvent(event: LoginContract.UiEvent) {
        when (event) {
            is LoginContract.UiEvent.UsernameChanged ->
                _uiState.update { it.copy(username = event.value, error = null) }
            is LoginContract.UiEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, error = null) }
            LoginContract.UiEvent.LoginClicked -> login()
            LoginContract.UiEvent.SignupClicked -> viewModelScope.launch {
                _sideEffect.send(LoginContract.SideEffect.NavigateToSignup)
            }
        }
    }

    private fun login() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            authRepository.login(
                username = _uiState.value.username.trim(),
                password = _uiState.value.password,
            )
            _sideEffect.send(LoginContract.SideEffect.NavigateToMovies)
        } catch (e: ResponseException) {
            val error = when (e.response.status) {
                HttpStatusCode.Unauthorized -> "Pogrešno korisničko ime ili lozinka"
                else -> "Greška servera (${e.response.status.value})"
            }
            _uiState.update { it.copy(isLoading = false, error = error) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = "Mrežna greška, pokušaj ponovo") }
        }
    }
}
