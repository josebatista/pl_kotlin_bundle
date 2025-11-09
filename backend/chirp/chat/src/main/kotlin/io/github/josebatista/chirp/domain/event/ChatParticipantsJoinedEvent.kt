package io.github.josebatista.chirp.domain.event

import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)
