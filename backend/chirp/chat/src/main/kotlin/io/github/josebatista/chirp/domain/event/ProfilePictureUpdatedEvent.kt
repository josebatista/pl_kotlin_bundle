package io.github.josebatista.chirp.domain.event

import io.github.josebatista.chirp.domain.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?,
)
