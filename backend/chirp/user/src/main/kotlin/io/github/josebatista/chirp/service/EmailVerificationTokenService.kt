package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.events.user.UserEvent
import io.github.josebatista.chirp.domain.exception.InvalidTokenException
import io.github.josebatista.chirp.domain.exception.UserNotFoundException
import io.github.josebatista.chirp.domain.model.EmailVerificationToken
import io.github.josebatista.chirp.infra.database.entity.EmailVerificationTokenEntity
import io.github.josebatista.chirp.infra.database.mappers.toEmailVerificationToken
import io.github.josebatista.chirp.infra.database.repositories.EmailVerificationTokenRepository
import io.github.josebatista.chirp.infra.database.repositories.UserRepository
import io.github.josebatista.chirp.infra.message_queue.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationTokenService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value($$"${chirp.email.verification.expiry-hours}") private val expiryHours: Long,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    fun resendVerificationEmail(email: String) {
        val token = createVerificationToken(email = email)
        if (token.user.hasEmailVerified) return
        eventPublisher.publish(
            event = UserEvent.RequestResendVerification(
                userId = token.user.id,
                email = token.user.email,
                username = token.user.username,
                verificationToken = token.token
            )
        )
    }

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email = email) ?: throw UserNotFoundException()
        emailVerificationTokenRepository.invalidateActiveTokensForUser(user = userEntity)
        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity
        )
        return emailVerificationTokenRepository.save(token).toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token = token)
            ?: throw InvalidTokenException(message = "Email token verification is invalid.")
        if (verificationToken.isUsed) throw InvalidTokenException(message = "Email verification token is already used.")
        if (verificationToken.isExpired) throw InvalidTokenException(message = "Email verification token has already expired.")
        emailVerificationTokenRepository.save(verificationToken.apply { usedAt = Instant.now() })
        userRepository.save(verificationToken.user.apply { hasVerifiedEmail = true })
        eventPublisher.publish(
            event = UserEvent.Verified(
                userId = verificationToken.user.id!!,
                email = verificationToken.user.email,
                username = verificationToken.user.username,
            )
        )
    }

    @Scheduled(cron = "0 0 3 * * * ")
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}
