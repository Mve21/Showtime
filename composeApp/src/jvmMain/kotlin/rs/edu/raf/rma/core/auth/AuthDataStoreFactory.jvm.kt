package rs.edu.raf.rma.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path.Companion.toPath
import rs.edu.raf.rma.core.auth.model.AuthData

actual fun createAuthDataStore(): DataStore<AuthData> {
    val path = System.getProperty("user.home") + "/.showtime/auth_data.json"
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = AuthDataSerializer,
            producePath = { path.toPath() },
        )
    )
}
