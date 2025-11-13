package io.github.josebatista.chirp.api.mappers

import io.github.josebatista.chirp.api.dto.DeviceTokenDto
import io.github.josebatista.chirp.api.dto.PlatformDto
import io.github.josebatista.chirp.api.dto.PlatformDto.ANDROID
import io.github.josebatista.chirp.api.dto.PlatformDto.IOS
import io.github.josebatista.chirp.domain.model.DeviceToken

fun DeviceToken.toDeviceTokenDto(): DeviceTokenDto = DeviceTokenDto(
    userId = userId,
    token = token,
    createdAt = createdAt
)

fun PlatformDto.toPlatform(): DeviceToken.Platform = when (this) {
    ANDROID -> DeviceToken.Platform.ANDROID
    IOS -> DeviceToken.Platform.IOS
}
