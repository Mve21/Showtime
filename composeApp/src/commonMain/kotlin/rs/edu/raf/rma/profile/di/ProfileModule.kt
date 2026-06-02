package rs.edu.raf.rma.profile.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.profile.ProfileViewModel

val profileModule = module {
    viewModelOf(::ProfileViewModel)
}
