package io.github.josebatista.chirp.infra.mappers

import io.github.josebatista.chirp.domain.model.DeviceToken
import io.github.josebatista.chirp.infra.database.entity.DeviceTokenEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken = DeviceToken(
    id = id,
    userId = userId,
    token = token,
    platform = platform.toPlatform(),
    createdAt = createdAt
)
