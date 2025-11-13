package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.expection.InvalidDeviceTokenException
import io.github.josebatista.chirp.domain.model.DeviceToken
import io.github.josebatista.chirp.domain.model.PushNotification
import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entity.DeviceTokenEntity
import io.github.josebatista.chirp.infra.database.repository.DeviceTokenRepository
import io.github.josebatista.chirp.infra.mappers.toDeviceToken
import io.github.josebatista.chirp.infra.mappers.toPlatformEntity
import io.github.josebatista.chirp.infra.push_notification.FirebasePushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
) {
    private companion object {
        val RETRY_DELAYS_SECONDS = listOf(
            30L,
            60L,
            120L,
            300L,
            600L
        )
        const val MAX_RETRY_AGE_MINUTES = 10L
    }

    private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun registerDevice(
        userId: UserId,
        token: String,
        platform: DeviceToken.Platform
    ): DeviceToken {
        val existing = deviceTokenRepository.findByToken(token = token)
        val trimmedToken = token.trim()
        if (existing == null && !firebasePushNotificationService.isValidToken(token = trimmedToken)) {
            throw InvalidDeviceTokenException()
        }
        return if (existing != null) {
            deviceTokenRepository.save(
                existing.apply {
                    this.userId = userId
                }
            )
        } else {
            deviceTokenRepository.save(
                DeviceTokenEntity(
                    userId = userId,
                    token = trimmedToken,
                    platform = platform.toPlatformEntity(),
                )
            )
        }.toDeviceToken()
    }

    @Transactional
    fun unregisterDevice(token: String) {
        deviceTokenRepository.deleteByToken(token.trim())
    }

    fun sendNewMessageNotification(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        senderUsername: String,
        message: String,
        chatId: ChatId
    ) {
        val deviceTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)
        if (deviceTokens.isEmpty()) {
            logger.info("No device tokens found fo $recipientUserIds")
            return
        }
        val recipients = deviceTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }
        val notification = PushNotification(
            title = "New message from $senderUsername",
            recipients = recipients,
            message = message,
            chatId = chatId,
            data = mapOf(
                "chatId" to chatId.toString(),
                "type" to "new_message"
            )
        )
        sendWithRetry(notification = notification)
    }

    fun sendWithRetry(
        notification: PushNotification,
        attempt: Int = 0
    ) {
        val result = firebasePushNotificationService.sendNotification(notification = notification)
        result.permanentFailures.forEach { deviceTokenRepository.deleteByToken(token = it.token) }
        if (result.temporaryFailures.isNotEmpty() && attempt < RETRY_DELAYS_SECONDS.size) {
            val retryNotification = notification.copy(
                recipients = result.temporaryFailures
            )
            scheduleRetry(notification = retryNotification, attempt = attempt + 1)
        }
        if (result.succeeded.isNotEmpty()) {
            logger.info("Successfully sent notification to ${result.succeeded.size} devices")
        }
    }

    private fun scheduleRetry(
        notification: PushNotification,
        attempt: Int
    ) {
        val delay = RETRY_DELAYS_SECONDS.getOrElse(index = attempt - 1) {
            RETRY_DELAYS_SECONDS.last()
        }
        val executeAt = Instant.now().plusSeconds(/* secondsToAdd = */ delay)
        val executeAtMillis = executeAt.toEpochMilli()
        val retryData = RetryData(
            notification = notification,
            attempt = attempt,
            createdAt = Instant.now()
        )
        retryQueue.compute(executeAtMillis) { _, retries ->
            (retries ?: mutableListOf()).apply { add(element = retryData) }
        }
        logger.info("Scheduled retry $attempt for ${notification.id} in $delay seconds")
    }

    @Scheduled(fixedDelay = 15_000L)
    fun processEntries() {
        val now = Instant.now()
        val nowMillis = now.toEpochMilli()
        val toProcess = retryQueue.headMap(/* toKey = */ nowMillis, /* inclusive = */ true)
        if (toProcess.isEmpty()) return
        val entries = toProcess.entries.toList()
        entries.forEach { (timeMillis, retries) ->
            retryQueue.remove(/* key = */ timeMillis)
            retries.forEach { retry ->
                try {
                    val age = Duration.between(/* startInclusive = */ retry.createdAt, /* endExclusive = */ now)
                    if (age.toMinutes() > MAX_RETRY_AGE_MINUTES) {
                        logger.warn("Dropping old retry (${age.toMinutes()} old)")
                        return@forEach
                    }
                    sendWithRetry(
                        notification = retry.notification,
                        attempt = retry.attempt
                    )
                } catch (e: Exception) {
                    logger.warn("Error processing retry ${retry.notification.id}", e)
                }
            }
        }
    }

    private data class RetryData(
        val notification: PushNotification,
        val attempt: Int,
        val createdAt: Instant
    )
}
