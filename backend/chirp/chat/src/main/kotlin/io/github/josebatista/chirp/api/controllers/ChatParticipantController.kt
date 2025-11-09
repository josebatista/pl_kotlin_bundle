package io.github.josebatista.chirp.api.controllers

import io.github.josebatista.chirp.api.dto.ChatParticipantDto
import io.github.josebatista.chirp.api.dto.ConfirmProfilePictureRequest
import io.github.josebatista.chirp.api.dto.PictureUploadResponse
import io.github.josebatista.chirp.api.mappers.toChatParticipantDto
import io.github.josebatista.chirp.api.mappers.toResponse
import io.github.josebatista.chirp.api.util.requestUserId
import io.github.josebatista.chirp.service.ChatParticipantService
import io.github.josebatista.chirp.service.ProfilePictureService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/participants")
class ChatParticipantController(
    private val chatParticipantService: ChatParticipantService,
    private val profilePictureService: ProfilePictureService
) {

    @GetMapping
    fun getChatParticipantByUsernameOrEmail(
        @RequestParam(required = false) query: String?
    ): ChatParticipantDto {
        val participant = if (query == null) {
            chatParticipantService.findChatParticipantById(requestUserId)
        } else {
            chatParticipantService.findChatParticipantByEmailOrUsername(query)
        }
        return participant?.toChatParticipantDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @PostMapping("/profile-picture-upload")
    fun getProfilePictureUploadUrl(
        @RequestParam("mimeType") mimeType: String
    ): PictureUploadResponse {
        return profilePictureService.generateUploadCredentials(
            userId = requestUserId,
            mimeType = mimeType
        ).toResponse()
    }

    @PostMapping("/confirm-profile-picture")
    fun confirmProfilePictureUpload(
        @Valid @RequestBody body: ConfirmProfilePictureRequest
    ) {
        profilePictureService.confirmProfilePictureUpload(
            userId = requestUserId,
            publicUrl = body.publicUrl
        )
    }

    @DeleteMapping("/profile-picture")
    fun deleteProfilePicture() {
        profilePictureService.deleteProfilePicture(userId = requestUserId)
    }
}
