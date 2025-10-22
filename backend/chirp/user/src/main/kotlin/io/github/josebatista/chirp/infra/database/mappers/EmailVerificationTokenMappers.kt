package io.github.josebatista.chirp.infra.database.mappers

import io.github.josebatista.chirp.domain.model.EmailVerificationToken
import io.github.josebatista.chirp.infra.database.entity.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken = EmailVerificationToken(
    id = id,
    token = token,
    user = user.toUser()
)
