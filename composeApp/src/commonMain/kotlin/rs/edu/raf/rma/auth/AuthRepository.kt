package rs.edu.raf.rma.auth

interface AuthRepository {
    suspend fun login(username: String, password: String)
    suspend fun signup(fullName: String, username: String, password: String)
}
