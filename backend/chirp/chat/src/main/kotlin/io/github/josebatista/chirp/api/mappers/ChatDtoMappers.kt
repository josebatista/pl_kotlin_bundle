package io.github.josebatista.chirp.api.mappers

import io.github.josebatista.chirp.api.dto.ChatDto
import io.github.josebatista.chirp.api.dto.ChatMessageDto
import io.github.josebatista.chirp.api.dto.ChatParticipantDto
import io.github.josebatista.chirp.domain.models.Chat
import io.github.josebatista.chirp.domain.models.ChatMessage
import io.github.josebatista.chirp.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto = ChatDto(
    id = id,
    participants = participants.map { it.toChatParticipantDto() },
    lastActivityAt = lastActivityAt,
    lastMessage = lastMessage?.toChatMessageDto(),
    creator = creator.toChatParticipantDto()
)

fun ChatMessage.toChatMessageDto(): ChatMessageDto = ChatMessageDto(
    id = id,
    chatId = chatId,
    content = content,
    createdAt = createdAt,
    senderId = sender.userId
)

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto = ChatParticipantDto(
    userId = userId,
    username = username,
    email = email,
    profilePictureUrl = profilePictureUrl
)
