package io.github.josebatista.chirp.api.mappers

import io.github.josebatista.chirp.api.dto.AuthenticatedUserDto
import io.github.josebatista.chirp.api.dto.UserDto
import io.github.josebatista.chirp.domain.model.AuthenticatedUser
import io.github.josebatista.chirp.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto = AuthenticatedUserDto(
    user = user.toUserDto(),
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun User.toUserDto(): UserDto = UserDto(
    id = id,
    email = email,
    username = username,
    hasVerifiedEmail = hasEmailVerified
)
