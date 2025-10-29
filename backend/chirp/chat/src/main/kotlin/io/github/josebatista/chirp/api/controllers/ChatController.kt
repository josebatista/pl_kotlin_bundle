package io.github.josebatista.chirp.api.controllers

import io.github.josebatista.chirp.api.dto.ChatDto
import io.github.josebatista.chirp.api.dto.CreateChatRequest
import io.github.josebatista.chirp.api.mappers.toChatDto
import io.github.josebatista.chirp.api.util.requestUserId
import io.github.josebatista.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserId = body.otherUserIds.toSet()
        ).toChatDto()
    }
}
