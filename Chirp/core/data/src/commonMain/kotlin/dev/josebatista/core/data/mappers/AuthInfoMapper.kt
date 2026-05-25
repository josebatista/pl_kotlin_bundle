package dev.josebatista.core.data.mappers

import dev.josebatista.core.data.dto.AuthInfoSerializable
import dev.josebatista.core.data.dto.UserSerializable
import dev.josebatista.core.domain.auth.AuthInfo
import dev.josebatista.core.domain.auth.User

fun AuthInfoSerializable.toDomain(): AuthInfo = AuthInfo(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = user.toDomain()
)

fun AuthInfo.toSerializable(): AuthInfoSerializable = AuthInfoSerializable(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = user.toSerializable()
)

fun UserSerializable.toDomain(): User = User(
    id = id,
    email = email,
    username = username,
    hasVerifiedEmail = hasVerifiedEmail,
    profilePictureUrl = profilePictureUrl
)

fun User.toSerializable(): UserSerializable = UserSerializable(
    id = id,
    email = email,
    username = username,
    hasVerifiedEmail = hasVerifiedEmail,
    profilePictureUrl = profilePictureUrl
)
