package rs.edu.raf.rma.networking.di

import org.koin.core.qualifier.StringQualifier

object Qualifiers {
    val Unauthenticated = StringQualifier("Unauthenticated")
    val Authenticated = StringQualifier("Authenticated")
}
