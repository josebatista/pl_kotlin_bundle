package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.exception.ChatParticipantNotFoundException
import io.github.josebatista.chirp.domain.exception.InvalidChatSizeException
import io.github.josebatista.chirp.domain.models.Chat
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entities.ChatEntity
import io.github.josebatista.chirp.infra.database.mappers.toChat
import io.github.josebatista.chirp.infra.database.repository.ChatParticipantRepository
import io.github.josebatista.chirp.infra.database.repository.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
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
}
