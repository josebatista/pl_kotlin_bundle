package io.github.josebatista.chirp.api.dto

import io.github.josebatista.chirp.domain.type.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean
)
