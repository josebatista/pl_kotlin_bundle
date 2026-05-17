package dev.josebatista.core.domain.auth

import dev.josebatista.core.domain.util.DataError
import dev.josebatista.core.domain.util.EmptyResult

interface AuthService {
    suspend fun register(
        username: String,
        email: String,
        password: String,
    ): EmptyResult<DataError.Remote>

    suspend fun resendVerificationEmail(
        email: String
    ): EmptyResult<DataError.Remote>
}
