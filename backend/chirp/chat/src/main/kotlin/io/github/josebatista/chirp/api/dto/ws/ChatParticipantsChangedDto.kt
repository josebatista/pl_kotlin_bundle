package io.github.josebatista.chirp.api.dto.ws

import io.github.josebatista.chirp.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId,
)
