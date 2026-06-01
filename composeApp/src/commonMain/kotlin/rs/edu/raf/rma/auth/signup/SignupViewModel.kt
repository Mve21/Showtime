package rs.edu.raf.rma.auth.signup

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

class SignupViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupContract.UiState())
    val uiState: StateFlow<SignupContract.UiState> = _uiState.asStateFlow()

    private val _sideEffect = Channel<SignupContract.SideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onEvent(event: SignupContract.UiEvent) {
        when (event) {
            is SignupContract.UiEvent.FullNameChanged ->
                _uiState.update { it.copy(fullName = event.value, error = null) }
            is SignupContract.UiEvent.UsernameChanged ->
                _uiState.update { it.copy(username = event.value, error = null) }
            is SignupContract.UiEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, error = null) }
            SignupContract.UiEvent.SignupClicked -> signup()
        }
    }

    private fun signup() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                authRepository.signup(
                    fullName = state.fullName.trim(),
                    username = state.username.trim(),
                    password = state.password,
                )
                _sideEffect.send(SignupContract.SideEffect.NavigateToMovies)
            } catch (e: ResponseException) {
                val error = when (e.response.status) {
                    HttpStatusCode.UnprocessableEntity,
                    HttpStatusCode.Conflict -> "Korisničko ime je već zauzeto"
                    else -> "Greška servera (${e.response.status.value})"
                }
                _uiState.update { it.copy(isLoading = false, error = error) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Mrežna greška, pokušaj ponovo") }
            }
        }
    }

    private fun validate(state: SignupContract.UiState): String? {
        if (state.fullName.isBlank()) return "Ime i prezime su obavezni"
        if (state.username.isBlank()) return "Korisničko ime je obavezno"
        if (!state.username.matches(Regex("[a-zA-Z0-9_]{3,}")))
            return "Korisničko ime: min 3 karaktera, samo slova, cifre i _"
        if (state.password.length < 8) return "Lozinka mora imati najmanje 8 karaktera"
        return null
    }
}
