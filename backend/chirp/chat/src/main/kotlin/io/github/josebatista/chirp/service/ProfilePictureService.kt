package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.event.ProfilePictureUpdatedEvent
import io.github.josebatista.chirp.domain.exception.ChatParticipantNotFoundException
import io.github.josebatista.chirp.domain.exception.InvalidProfilePictureException
import io.github.josebatista.chirp.domain.models.ProfilePictureUploadCredentials
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.repository.ChatParticipantRepository
import io.github.josebatista.chirp.infra.storage.SupabaseStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfilePictureService(
    private val supabaseStorageService: SupabaseStorageService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @param:Value($$"${supabase.url}") private val supabaseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun generateUploadCredentials(
        userId: UserId,
        mimeType: String,
    ): ProfilePictureUploadCredentials {
        return supabaseStorageService.generateSignedUploadUrl(
            userId = userId,
            mimeType = mimeType
        )
    }

    @Transactional
    fun deleteProfilePicture(userId: UserId) {
        val participant = chatParticipantRepository.findByIdOrNull(id = userId)
            ?: throw ChatParticipantNotFoundException(id = userId)
        participant.profilePictureUrl?.let { url ->
            chatParticipantRepository.save(
                /* entity = */ participant.apply { profilePictureUrl = null }
            )
            supabaseStorageService.deleteFile(url = url)
            applicationEventPublisher.publishEvent(
                /* event = */ ProfilePictureUpdatedEvent(
                    userId = userId,
                    newUrl = null
                )
            )
        }
    }

    @Transactional
    fun confirmProfilePictureUpload(userId: UserId, publicUrl: String) {
        if (!publicUrl.startsWith(prefix = supabaseUrl)) throw InvalidProfilePictureException(message = "Invalid profile picture url.")
        val participant = chatParticipantRepository.findByIdOrNull(id = userId)
            ?: throw ChatParticipantNotFoundException(id = userId)
        val oldUrl = participant.profilePictureUrl
        chatParticipantRepository.save(
            /* entity = */ participant.apply { profilePictureUrl = publicUrl }
        )
        try {
            oldUrl?.let { supabaseStorageService.deleteFile(url = it) }
        } catch (e: Exception) {
            logger.warn("Deleting old picture for $userId failed.", e)
        }
        applicationEventPublisher.publishEvent(
            /* event = */ ProfilePictureUpdatedEvent(
                userId = userId,
                newUrl = publicUrl
            )
        )
    }
}
