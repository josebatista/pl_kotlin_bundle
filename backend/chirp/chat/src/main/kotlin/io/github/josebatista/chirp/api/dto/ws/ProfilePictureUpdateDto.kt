package io.github.josebatista.chirp.api.dto.ws

import io.github.josebatista.chirp.domain.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?,
)
