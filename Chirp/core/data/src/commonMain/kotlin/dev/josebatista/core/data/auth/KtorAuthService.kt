package dev.josebatista.core.data.auth

import dev.josebatista.core.data.dto.requests.EmailRequest
import dev.josebatista.core.data.dto.requests.RegisterRequest
import dev.josebatista.core.data.networking.get
import dev.josebatista.core.data.networking.post
import dev.josebatista.core.domain.auth.AuthService
import dev.josebatista.core.domain.util.DataError
import dev.josebatista.core.domain.util.EmptyResult
import io.ktor.client.HttpClient

class KtorAuthService(
    private val httpClient: HttpClient
) : AuthService {
    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/register",
            body = RegisterRequest(
                username = username,
                email = email,
                password = password
            )
        )
    }

    override suspend fun resendVerificationEmail(email: String): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "/auth/resend-verification",
            body = EmailRequest(email = email)
        )
    }

    override suspend fun verifyEmail(token: String): EmptyResult<DataError.Remote> {
        return httpClient.get(route = "/auth/verify", queryParams = mapOf("token" to token))
    }
}
