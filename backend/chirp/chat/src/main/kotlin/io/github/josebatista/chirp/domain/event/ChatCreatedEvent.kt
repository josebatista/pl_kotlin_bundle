package io.github.josebatista.chirp.domain.event

import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.UserId

data class ChatCreatedEvent(
    val chatId: ChatId,
    val participantIds: List<UserId>
)
