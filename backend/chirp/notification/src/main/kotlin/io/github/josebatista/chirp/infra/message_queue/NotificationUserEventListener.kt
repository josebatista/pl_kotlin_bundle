package io.github.josebatista.chirp.infra.message_queue

import io.github.josebatista.chirp.domain.events.user.UserEvent
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class NotificationUserEventListener {

    @RabbitListener(queues = [MessageQueue.NOTIFICATION_USER_EVENTS])
    @Transactional
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Created -> println("User Created!")
            is UserEvent.RequestResendVerification -> println("Request resend verification!")
            is UserEvent.RequestResetPassword -> println("Request reset password!")
            else -> Unit
        }
    }
}
