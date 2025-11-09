package io.github.josebatista.chirp.domain.models

import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.ChatMessageId
import java.time.Instant

data class ChatMessage(
    val id: ChatMessageId,
    val chatId: ChatId,
    val sender: ChatParticipant,
    val content: String,
    val createdAt: Instant,
)
