package io.github.josebatista.chirp.domain.event

import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.UserId

data class ChatParticipantsLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)
