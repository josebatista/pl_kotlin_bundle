package io.github.josebatista.chirp.api.dto

import io.github.josebatista.chirp.domain.type.UserId
import java.time.Instant

data class DeviceTokenDto(
    val userId: UserId,
    val token: String,
    val createdAt: Instant
)
