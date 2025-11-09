package io.github.josebatista.chirp.api.dto

import io.github.josebatista.chirp.domain.type.UserId

data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?,
)
