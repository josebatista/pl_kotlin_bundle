package io.github.josebatista.chirp.domain.model

import io.github.josebatista.chirp.domain.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean
)
