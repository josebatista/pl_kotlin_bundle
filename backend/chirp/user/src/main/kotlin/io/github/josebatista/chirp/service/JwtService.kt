package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.exception.InvalidTokenException
import io.github.josebatista.chirp.domain.type.UserId
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import kotlin.io.encoding.Base64

@Service
class JwtService(
    @Value($$"${jwt.secret}") secretBase64: String,
    @Value($$"${jwt.expiration-minutes}") expirationMinutes: Int
) {

    private val secretKey = Keys.hmacShaKeyFor(
        Base64.decode(secretBase64)
    )
    private val accessTokenValidityInMs = expirationMinutes * 60 * 1000L
    val refreshTokenValidityMs = 30 * 24 * 60 * 60 * 1000L

    fun generateAccessToken(userId: UserId): String = generateToken(
        userId = userId,
        type = ACCESS_TYPE,
        expiry = accessTokenValidityInMs
    )

    fun generateRefreshToken(userId: UserId): String = generateToken(
        userId = userId,
        type = REFRESH_TYPE,
        expiry = refreshTokenValidityMs
    )

    fun validateAccessToken(token: String): Boolean = validateTokenByClaimType(
        token = token,
        claimType = ACCESS_TYPE
    )

    fun validateRefreshToken(token: String): Boolean = validateTokenByClaimType(
        token = token,
        claimType = REFRESH_TYPE
    )

    fun getUserIdFromToken(token: String): UserId {
        val claims = parseAllClaims(token = token) ?: throw InvalidTokenException(
            message = "The attached JWT token is not valid!"
        )
        return UUID.fromString(claims.subject)
    }

    private fun generateToken(
        userId: UserId,
        type: String,
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_TYPE, type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith(BEARER_PREFIX)) token.removePrefix(BEARER_PREFIX) else token
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (_: Exception) {
            null
        }
    }

    private fun validateTokenByClaimType(token: String, claimType: String): Boolean {
        val claims = parseAllClaims(token = token) ?: return false
        val tokenType = claims[CLAIM_TYPE] as? String ?: return false
        return tokenType == claimType
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
        const val CLAIM_TYPE = "type"
        const val ACCESS_TYPE = "access"
        const val REFRESH_TYPE = "refresh"
    }
}
