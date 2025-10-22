package io.github.josebatista.chirp.api.dto

import io.github.josebatista.chirp.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @field:NotBlank
    val oldPassword: String,

    @field:Password
    val newPassword: String
)
