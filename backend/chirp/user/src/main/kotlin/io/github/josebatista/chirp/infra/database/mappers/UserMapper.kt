package io.github.josebatista.chirp.infra.database.mappers

import io.github.josebatista.chirp.domain.model.User
import io.github.josebatista.chirp.infra.database.entity.UserEntity

fun UserEntity.toUser(): User = User(
    id = id!!,
    username = username,
    email = email,
    hasEmailVerified = hasVerifiedEmail
)
