package io.github.josebatista.chirp.infra.database.mappers

import io.github.josebatista.chirp.domain.models.Chat
import io.github.josebatista.chirp.domain.models.ChatMessage
import io.github.josebatista.chirp.domain.models.ChatParticipant
import io.github.josebatista.chirp.infra.database.entities.ChatEntity
import io.github.josebatista.chirp.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat = Chat(
    id = id!!,
    participants = participants.map { it.toChatParticipant() }.toSet(),
    lastMessage = lastMessage,
    creator = creator.toChatParticipant(),
    lastActivityAt = lastMessage?.createdAt ?: createdAt,
    createdAt = createdAt
)

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant = ChatParticipant(
    userId = userId,
    username = username,
    email = email,
    profilePictureUrl = profilePictureUrl
)
