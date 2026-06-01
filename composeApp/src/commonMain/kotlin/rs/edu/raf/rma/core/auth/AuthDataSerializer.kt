package rs.edu.raf.rma.core.auth

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import rs.edu.raf.rma.core.auth.model.AuthData

object AuthDataSerializer : OkioSerializer<AuthData> {
    override val defaultValue: AuthData = AuthData()

    override suspend fun readFrom(source: BufferedSource): AuthData {
        return try {
            Json.decodeFromString(AuthData.serializer(), source.readUtf8())
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: AuthData, sink: BufferedSink) {
        sink.writeUtf8(Json.encodeToString(AuthData.serializer(), t))
    }
}
