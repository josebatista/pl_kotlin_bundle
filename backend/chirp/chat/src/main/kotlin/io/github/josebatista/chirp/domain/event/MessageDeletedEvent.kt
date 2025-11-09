package io.github.josebatista.chirp.domain.event

import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId
)
