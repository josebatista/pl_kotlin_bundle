package io.github.josebatista.chirp.api.dto

import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.ChatMessageId
import io.github.josebatista.chirp.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val content: String,
    val createdAt: Instant,
    val senderId: UserId,
)
