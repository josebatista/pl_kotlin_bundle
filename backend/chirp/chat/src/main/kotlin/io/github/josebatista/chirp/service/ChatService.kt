package io.github.josebatista.chirp.service

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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {
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
        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants
            )
        ).toChat(lastMessage = null)
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userId: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(id = chatId) ?: throw ChatNotFoundException()
        val isRequestingUserInChat = chat.participants.any { it.userId == requestUserId }
        if (!isRequestingUserInChat) throw ForbiddenException()
        val users = userId.map { userId ->
            chatParticipantRepository.findByIdOrNull(id = userId) ?: throw ChatParticipantNotFoundException(id = userId)
        }
        return chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage = lastMessageForChat(chatId = chatId))
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
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(chatIds = setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}
