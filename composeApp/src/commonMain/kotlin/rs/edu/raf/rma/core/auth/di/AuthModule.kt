package rs.edu.raf.rma.core.auth.di

import androidx.datastore.core.DataStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.auth.AuthRepository
import rs.edu.raf.rma.auth.AuthRepositoryImpl
import rs.edu.raf.rma.auth.login.LoginViewModel
import rs.edu.raf.rma.auth.signup.SignupViewModel
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.createAuthDataStore
import rs.edu.raf.rma.core.auth.model.AuthData

val authModule = module {
    single<DataStore<AuthData>> { createAuthDataStore() }
    single { AuthStore(persistence = get()) }
    single<AuthRepository> { AuthRepositoryImpl(moviesApi = get(), authStore = get(), appDatabase = get()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::SignupViewModel)
}
