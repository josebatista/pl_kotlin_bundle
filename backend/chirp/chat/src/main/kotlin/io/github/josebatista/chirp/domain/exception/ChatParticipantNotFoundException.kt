package io.github.josebatista.chirp.domain.exception

import io.github.josebatista.chirp.domain.type.UserId

class ChatParticipantNotFoundException(
    id: UserId
) : RuntimeException("The chat participant with ID $id was not found")
