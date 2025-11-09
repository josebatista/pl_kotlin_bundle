package io.github.josebatista.chirp.api.dto.ws

import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.ChatMessageId

data class DeleteMessageDto(
    val chatId: ChatId,
    val messageId: ChatMessageId
)
