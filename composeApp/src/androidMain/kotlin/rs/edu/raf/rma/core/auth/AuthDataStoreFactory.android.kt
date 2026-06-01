package rs.edu.raf.rma.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path.Companion.toPath
import rs.edu.raf.rma.AppContextHolder
import rs.edu.raf.rma.core.auth.model.AuthData

actual fun createAuthDataStore(): DataStore<AuthData> {
    val path = AppContextHolder.appContext
        .filesDir
        .resolve("datastore/auth_data.json")
        .absolutePath
    return DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = AuthDataSerializer,
            producePath = { path.toPath() },
        )
    )
}
