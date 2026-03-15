package dev.josebatista.core.data.di

import dev.josebatista.core.data.auth.KtorAuthService
import dev.josebatista.core.data.logging.KermitLogger
import dev.josebatista.core.data.networking.HttpClientFactory
import dev.josebatista.core.domain.auth.AuthService
import dev.josebatista.core.domain.logging.ChirpLogger
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformCoreDataModule: Module

val coreDataModule = module {
    includes(platformCoreDataModule)
    single<ChirpLogger> { KermitLogger }
    single {
        HttpClientFactory(chirpLogger = get()).create(engine = get())
    }
//    single<AuthService> { KtorAuthService(httpClient = get()) }
    singleOf(::KtorAuthService) bind AuthService::class
}
