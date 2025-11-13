package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.api.dto.ChatMessageDto
import io.github.josebatista.chirp.api.mappers.toChatMessageDto
import io.github.josebatista.chirp.domain.event.ChatCreatedEvent
import io.github.josebatista.chirp.domain.event.ChatParticipantsJoinedEvent
import io.github.josebatista.chirp.domain.event.ChatParticipantsLeftEvent
import io.github.josebatista.chirp.domain.exception.ChatNotFoundException
import io.github.josebatista.chirp.domain.exception.ChatParticipantNotFoundException
import io.github.josebatista.chirp.domain.exception.ForbiddenException
import io.github.josebatista.chirp.domain.exception.InvalidChatSizeException
import io.github.josebatista.chirp.domain.models.Chat
import io.github.josebatista.chirp.domain.models.ChatMessage
import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entities.ChatEntity
import io.github.josebatista.chirp.infra.database.mappers.toChat
import io.github.josebatista.chirp.infra.database.mappers.toChatMessage
import io.github.josebatista.chirp.infra.database.repository.ChatMessageRepository
import io.github.josebatista.chirp.infra.database.repository.ChatParticipantRepository
import io.github.josebatista.chirp.infra.database.repository.ChatRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository.findByChatIdBefore(
            chatId = chatId,
            before = before ?: Instant.now(),
            pageable = PageRequest.of(0, pageSize)
        )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    fun getChatById(
        chatId: ChatId,
        requestUserId: UserId
    ): Chat? {
        return chatRepository.findChatById(id = chatId, userId = requestUserId)
            ?.toChat(lastMessageForChat(chatId = chatId))
    }

    fun findChatsByUser(userId: UserId): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId = userId)
        val chatIds = chatEntities.mapNotNull { it.id }
        val latestMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds = chatIds.toSet())
            .associateBy { it.chatId }
        return chatEntities
            .map {
                it.toChat(
                    lastMessage = latestMessages[it.id]?.toChatMessage()
                )
            }
            .sortedByDescending { it.lastActivityAt }
    }

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserId: Set<UserId>,
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserId
        )
        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) throw InvalidChatSizeException()
        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)
        return chatRepository.saveAndFlush(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat(lastMessage = null).also { entity ->
            applicationEventPublisher.publishEvent(
                ChatCreatedEvent(
                    chatId = entity.id,
                    participantIds = entity.participants.map { it.userId }
                )
            )
        }
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(id = chatId) ?: throw ChatNotFoundException()
        val isRequestingUserInChat = chat.participants.any { it.userId == requestUserId }
        if (!isRequestingUserInChat) throw ForbiddenException()
        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(id = userId) ?: throw ChatParticipantNotFoundException(id = userId)
        }
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage = lastMessageForChat(chatId = chatId))
        applicationEventPublisher.publishEvent(
            ChatParticipantsJoinedEvent(
                chatId = chatId,
                userIds = userIds
            )
        )
        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(id = chatId) ?: throw ChatNotFoundException()
        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(id = userId)
        val newParticipantsSize = chat.participants.size - 1
        if (newParticipantsSize == 0) {
            chatRepository.deleteById(userId)
            return
        }
        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )
        applicationEventPublisher.publishEvent(
            ChatParticipantsLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(chatIds = setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}
