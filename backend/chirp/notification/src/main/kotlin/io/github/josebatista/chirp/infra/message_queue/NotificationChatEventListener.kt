package io.github.josebatista.chirp.infra.message_queue

import io.github.josebatista.chirp.domain.events.chat.ChatEvent
import io.github.josebatista.chirp.service.PushNotificationService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationChatEventListener(
    private val pushNotificationService: PushNotificationService
) {

    @RabbitListener(queues = [MessageQueue.NOTIFICATION_CHAT_EVENTS])
    @Transactional
    fun handleChatEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.NewMessage -> pushNotificationService.sendNewMessageNotification(
                recipientUserIds = event.recipientIds.toList(),
                senderUserId = event.senderId,
                senderUsername = event.senderUsername,
                message = event.message,
                chatId = event.chatId
            )

        }
    }
}
