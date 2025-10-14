package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.exception.EncodePasswordException
import io.github.josebatista.chirp.domain.exception.InvalidCredentialsException
import io.github.josebatista.chirp.domain.exception.InvalidTokenException
import io.github.josebatista.chirp.domain.exception.UserAlreadyExistsException
import io.github.josebatista.chirp.domain.exception.UserNotFoundException
import io.github.josebatista.chirp.domain.model.AuthenticatedUser
import io.github.josebatista.chirp.domain.model.User
import io.github.josebatista.chirp.domain.model.UserId
import io.github.josebatista.chirp.infra.database.entity.RefreshTokenEntity
import io.github.josebatista.chirp.infra.database.entity.UserEntity
import io.github.josebatista.chirp.infra.database.mappers.toUser
import io.github.josebatista.chirp.infra.database.repositories.RefreshTokenRepository
import io.github.josebatista.chirp.infra.database.repositories.UserRepository
import io.github.josebatista.chirp.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    fun register(email: String, username: String, password: String): User {
        userRepository.findByEmailOrUsername(email = email, username = username)
            ?.let { throw UserAlreadyExistsException() }
        val encodedPassword = passwordEncoder.encode(password) ?: throw EncodePasswordException()
        return userRepository.save(
            UserEntity(
                email = email,
                username = username,
                hashedPassword = encodedPassword
            )
        ).toUser()
    }

    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email = email.trim())
        // Always perform password check to prevent timing attacks
        // If user doesn't exist, compare against dummy hash to maintain constant time
        val passwordHash = user?.hashedPassword ?: DUMMY_PASSWORD_HASH
        val passwordMatches = passwordEncoder.matches(password, passwordHash)
        if (user == null || !passwordMatches) {
            throw InvalidCredentialsException()
        }
        // TODO: check for verified email.
        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId = userId)
            val refreshToken = jwtService.generateRefreshToken(userId = userId)
            storeRefreshToken(userId = userId, token = refreshToken)
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refreshToken(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(token = refreshToken)) {
            throw InvalidTokenException(message = INVALID_REFRESH_TOKEN_MESSAGE)
        }
        val userId = jwtService.getUserIdFromToken(token = refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val hashed = hashToken(token = refreshToken)
        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(userId = userId, hashedToken = hashed)
                ?: throw InvalidTokenException(message = INVALID_REFRESH_TOKEN_MESSAGE)
            refreshTokenRepository.deleteByUserIdAndHashedToken(userId = userId, hashedToken = hashed)
            val accessToken = jwtService.generateAccessToken(userId = userId)
            val refreshToken = jwtService.generateRefreshToken(userId = userId)
            storeRefreshToken(userId = userId, token = refreshToken)
            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFromToken(token = refreshToken)
        val hashed = hashToken(token = refreshToken)
        refreshTokenRepository.deleteByUserIdAndHashedToken(userId = userId, hashedToken = hashed)
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token = token)
        val expiry = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiry)
        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance(DIGEST_ALGORITHM)
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    private companion object {
        const val DIGEST_ALGORITHM = "SHA-256"

        const val INVALID_REFRESH_TOKEN_MESSAGE = "Invalid refresh token"

        // Pre-computed BCrypt hash of "dummy-password-to-prevent-timing-attack"
        // Uses cost factor 10 (2^10 rounds) to match production password encoder settings
        // This ensures constant-time authentication regardless of whether user exists
        const val DUMMY_PASSWORD_HASH = $$"$2a$10$N2qo8uLOitkxg2ZNRZoMye/VJKMf.zJ3/IJcONPm8JxQz5JZhqYBW"
    }
}
