package io.github.josebatista.chirp.api.dto

import io.github.josebatista.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,

    @field:Password
    val newPassword: String,
)
