package io.github.josebatista.chirp.infra.message_queue

import io.github.josebatista.chirp.domain.events.user.UserEvent
import io.github.josebatista.chirp.domain.models.ChatParticipant
import io.github.josebatista.chirp.service.ChatParticipantService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService
) {

    @RabbitListener(queues = [MessageQueue.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Verified -> chatParticipantService.createChatParticipant(
                chatParticipant = ChatParticipant(
                    userId = event.userId,
                    username = event.username,
                    email = event.email,
                    profilePictureUrl = null
                )
            )

            else -> Unit
        }
    }
}
