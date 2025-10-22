package io.github.josebatista.chirp.infra.security

import java.security.SecureRandom
import java.util.Base64

object TokenGenerator {
    fun generateToken(): String {
        val bytes = ByteArray(32) { 0 }
        SecureRandom().apply { nextBytes(bytes) }
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }
}
