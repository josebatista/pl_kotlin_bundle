package io.github.josebatista.chirp.infra.mappers

import io.github.josebatista.chirp.domain.model.DeviceToken
import io.github.josebatista.chirp.infra.database.entity.PlatformEntity

fun DeviceToken.Platform.toPlatformEntity(): PlatformEntity {
    return when (this) {
        DeviceToken.Platform.ANDROID -> PlatformEntity.ANDROID
        DeviceToken.Platform.IOS -> PlatformEntity.IOS
    }
}

fun PlatformEntity.toPlatform(): DeviceToken.Platform {
    return when (this) {
        PlatformEntity.ANDROID -> DeviceToken.Platform.ANDROID
        PlatformEntity.IOS -> DeviceToken.Platform.IOS
    }
}
