package rs.edu.raf.rma.core.auth

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.core.auth.model.asAuthState

class AuthStore(private val persistence: DataStore<AuthData>) {

    val authState: StateFlow<AuthState> = persistence.data
        .map { it.asAuthState() }
        .distinctUntilChanged()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Unauthenticated,
        )

    suspend fun setAuthData(authData: AuthData) {
        persistence.updateData { authData }
    }

    suspend fun clearAuthData() {
        persistence.updateData { AuthData() }
    }

    suspend fun awaitInitialAuthState(): AuthState =
        persistence.data.map { it.asAuthState() }.first()
}
