package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.exception.InvalidTokenException
import io.github.josebatista.chirp.domain.exception.UserNotFoundException
import io.github.josebatista.chirp.domain.model.EmailVerificationToken
import io.github.josebatista.chirp.infra.database.entity.EmailVerificationTokenEntity
import io.github.josebatista.chirp.infra.database.mappers.toEmailVerificationToken
import io.github.josebatista.chirp.infra.database.repositories.EmailVerificationTokenRepository
import io.github.josebatista.chirp.infra.database.repositories.UserRepository
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
    @param:Value($$"${chirp.email.verification.expiry-hours}") private val expiryHours: Long
) {

    @Transactional
    fun createToken(email: String): EmailVerificationToken {
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
    }

    @Scheduled(cron = "0 0 3 * * * ")
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(Instant.now())
    }
}
