package rs.edu.raf.rma.auth.signup

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

class SignupViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignupContract.UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: SignupContract.UiState.() -> SignupContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val events = MutableSharedFlow<SignupContract.UiEvent>()

    fun setEvent(event: SignupContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    private val _effects = MutableSharedFlow<SignupContract.SideEffect>()
    val sideEffects = _effects.asSharedFlow()
    private fun setEffect(effect: SignupContract.SideEffect) {
        viewModelScope.launch { _effects.emit(effect) }
    }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is SignupContract.UiEvent.FullNameChanged ->
                        setState { copy(fullName = event.value, error = null) }
                    is SignupContract.UiEvent.UsernameChanged ->
                        setState { copy(username = event.value, error = null) }
                    is SignupContract.UiEvent.PasswordChanged ->
                        setState { copy(password = event.value, error = null) }
                    SignupContract.UiEvent.SignupClicked -> signup()
                }
            }
        }
    }

    private fun signup() {
        val state = _state.value
        val validationError = validate(state)
        if (validationError != null) {
            setState { copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            try {
                authRepository.signup(
                    fullName = state.fullName.trim(),
                    username = state.username.trim(),
                    password = state.password,
                )
                setEffect(SignupContract.SideEffect.NavigateToMovies)
            } catch (e: ResponseException) {
                val error = when (e.response.status) {
                    HttpStatusCode.UnprocessableEntity,
                    HttpStatusCode.Conflict -> "Korisničko ime je već zauzeto"
                    else -> "Greška servera (${e.response.status.value})"
                }
                setState { copy(isLoading = false, error = error) }
            } catch (e: Exception) {
                setState { copy(isLoading = false, error = "Mrežna greška, pokušaj ponovo") }
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
