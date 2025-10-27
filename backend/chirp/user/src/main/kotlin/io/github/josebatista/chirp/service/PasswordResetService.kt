package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.events.user.UserEvent
import io.github.josebatista.chirp.domain.exception.EncodePasswordException
import io.github.josebatista.chirp.domain.exception.InvalidCredentialsException
import io.github.josebatista.chirp.domain.exception.InvalidTokenException
import io.github.josebatista.chirp.domain.exception.SamePasswordException
import io.github.josebatista.chirp.domain.exception.UserNotFoundException
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entity.PasswordResetTokenEntity
import io.github.josebatista.chirp.infra.database.repositories.PasswordResetTokenRepository
import io.github.josebatista.chirp.infra.database.repositories.RefreshTokenRepository
import io.github.josebatista.chirp.infra.database.repositories.UserRepository
import io.github.josebatista.chirp.infra.message_queue.EventPublisher
import io.github.josebatista.chirp.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    @param:Value($$"${chirp.email.reset-password.expiry-minutes}") private val expiryMinutes: Long,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email = email) ?: return
        passwordResetTokenRepository.invalidateActiveTokensForUser(user = user)
        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(token)
        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.id!!,
                email = user.email,
                username = user.username,
                passwordResetToken = token.token,
                expiresInMinutes = expiryMinutes
            )
        )
    }

    @Transactional
    fun resetToken(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException(message = "Invalid reset password token.")
        if (resetToken.isUsed) throw InvalidTokenException(message = "Reset token is already used.")
        if (resetToken.isExpired) throw InvalidTokenException(message = "Reset token has already expired.")
        val user = resetToken.user
        if (passwordEncoder.matches(
                rawPassword = newPassword,
                hashedPassword = user.hashedPassword
            )
        ) throw SamePasswordException()
        val hashedNewPassword = passwordEncoder.encode(rawPassword = newPassword) ?: throw EncodePasswordException()
        userRepository.save(user.apply { this.hashedPassword = hashedNewPassword })
        passwordResetTokenRepository.save(resetToken.apply { this.usedAt = Instant.now() })
        refreshTokenRepository.deleteByUserId(userId = user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        if (!passwordEncoder.matches(
                rawPassword = oldPassword,
                hashedPassword = user.hashedPassword
            )
        ) throw InvalidCredentialsException()
        if (oldPassword == newPassword) throw SamePasswordException()
        refreshTokenRepository.deleteByUserId(userId = user.id!!)
        val newHashedPassword = passwordEncoder.encode(rawPassword = newPassword) ?: throw EncodePasswordException()
        userRepository.save(user.apply { this.hashedPassword = newHashedPassword })
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(now = Instant.now())
    }
}
