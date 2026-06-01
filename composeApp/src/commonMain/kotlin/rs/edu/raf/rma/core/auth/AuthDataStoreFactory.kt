package rs.edu.raf.rma.core.auth

import androidx.datastore.core.DataStore
import rs.edu.raf.rma.core.auth.model.AuthData

expect fun createAuthDataStore(): DataStore<AuthData>
