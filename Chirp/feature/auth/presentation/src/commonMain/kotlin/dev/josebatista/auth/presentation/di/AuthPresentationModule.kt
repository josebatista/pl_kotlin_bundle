package dev.josebatista.auth.presentation.di

import dev.josebatista.auth.presentation.email_verification.EmailVerificationViewModel
import dev.josebatista.auth.presentation.login.LoginViewModel
import dev.josebatista.auth.presentation.register.RegisterViewModel
import dev.josebatista.auth.presentation.register_success.RegisterSuccessViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::EmailVerificationViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::RegisterSuccessViewModel)
}
