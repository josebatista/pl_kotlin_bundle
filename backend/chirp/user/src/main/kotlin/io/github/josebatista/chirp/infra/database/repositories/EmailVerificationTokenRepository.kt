package io.github.josebatista.chirp.infra.database.repositories

import io.github.josebatista.chirp.infra.database.entity.EmailVerificationTokenEntity
import io.github.josebatista.chirp.infra.database.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)
    fun findByUserAndUsedAtIsNull(user: UserEntity): List<EmailVerificationTokenEntity>
}
