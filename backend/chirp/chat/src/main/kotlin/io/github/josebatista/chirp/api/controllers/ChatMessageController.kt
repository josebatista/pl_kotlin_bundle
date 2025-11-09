package io.github.josebatista.chirp.api.controllers

import io.github.josebatista.chirp.api.util.requestUserId
import io.github.josebatista.chirp.domain.type.ChatMessageId
import io.github.josebatista.chirp.service.ChatMessageService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class ChatMessageController(
    private val chatMessageService: ChatMessageService
) {

    @DeleteMapping("/{messageId}")
    fun deleteMessage(
        @PathVariable("messageId") messageId: ChatMessageId
    ) {
        chatMessageService.deleteMessage(messageId = messageId, requestUserId = requestUserId)
    }
}
