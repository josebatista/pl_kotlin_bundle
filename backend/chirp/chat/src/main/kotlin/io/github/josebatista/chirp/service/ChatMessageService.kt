package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.event.MessageDeletedEvent
import io.github.josebatista.chirp.domain.events.chat.ChatEvent
import io.github.josebatista.chirp.domain.exception.ChatNotFoundException
import io.github.josebatista.chirp.domain.exception.ChatParticipantNotFoundException
import io.github.josebatista.chirp.domain.exception.ForbiddenException
import io.github.josebatista.chirp.domain.exception.MessageNotFoundException
import io.github.josebatista.chirp.domain.models.ChatMessage
import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.ChatMessageId
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entities.ChatMessageEntity
import io.github.josebatista.chirp.infra.database.mappers.toChatMessage
import io.github.josebatista.chirp.infra.database.repository.ChatMessageRepository
import io.github.josebatista.chirp.infra.database.repository.ChatParticipantRepository
import io.github.josebatista.chirp.infra.database.repository.ChatRepository
import io.github.josebatista.chirp.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher,
    private val messageCacheEvictionHelper: MessageCacheEvictionHelper,
) {

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null
    ): ChatMessage {
        val chat = chatRepository.findChatById(id = chatId, userId = senderId) ?: throw ChatNotFoundException()
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(id = senderId)
        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId ?: UUID.randomUUID(),
                content = content,
                chatId = chatId,
                chat = chat,
                sender = sender,
            )
        ).toChatMessage()
        eventPublisher.publish(
            event = ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.content,
            )
        )
        return savedMessage
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId) ?: throw MessageNotFoundException(messageId)
        if (message.sender.userId != requestUserId) throw ForbiddenException()
        chatMessageRepository.delete(message)
        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = messageId
            )
        )
        messageCacheEvictionHelper.evictMessagesCache(chatId = message.chatId)
    }
}
