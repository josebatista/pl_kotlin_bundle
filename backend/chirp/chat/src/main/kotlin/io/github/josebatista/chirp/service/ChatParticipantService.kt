package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.models.ChatParticipant
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.mappers.toChatParticipant
import io.github.josebatista.chirp.infra.database.mappers.toChatParticipantEntity
import io.github.josebatista.chirp.infra.database.repository.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun createChatParticipant(
        chatParticipant: ChatParticipant
    ) {
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(query: String): ChatParticipant? {
        val normalizedQuery = query.lowercase().trim()
        return chatParticipantRepository.findByEmailOrUsername(normalizedQuery)?.toChatParticipant()
    }
}
