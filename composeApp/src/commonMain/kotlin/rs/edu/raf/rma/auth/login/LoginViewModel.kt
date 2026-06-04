package rs.edu.raf.rma.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.auth.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: LoginContract.UiState.() -> LoginContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<LoginContract.UiEvent>()

    fun setEvent(event: LoginContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<LoginContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: LoginContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is LoginContract.UiEvent.UsernameChanged ->
                        setState { copy(username = event.value, error = null) }
                    is LoginContract.UiEvent.PasswordChanged ->
                        setState { copy(password = event.value, error = null) }
                    LoginContract.UiEvent.LoginClicked -> login()
                    LoginContract.UiEvent.SignupClicked ->
                        setEffect(LoginContract.SideEffect.NavigateToSignup)
                }
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                authRepository.login(
                    username = _state.value.username.trim(),
                    password = _state.value.password,
                )
                setEffect(LoginContract.SideEffect.NavigateToMovies)
            } catch (e: ResponseException) {
                val error = when (e.response.status) {
                    HttpStatusCode.Unauthorized -> "Pogrešno korisničko ime ili lozinka"
                    else -> "Greška servera (${e.response.status.value})"
                }
                setState { copy(isLoading = false, error = error) }
            } catch (e: Exception) {
                setState { copy(isLoading = false, error = "Mrežna greška, pokušaj ponovo") }
            }
        }
    }
}
