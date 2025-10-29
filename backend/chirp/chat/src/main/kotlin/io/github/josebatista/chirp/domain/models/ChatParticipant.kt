package io.github.josebatista.chirp.domain.models

import io.github.josebatista.chirp.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?,
)
