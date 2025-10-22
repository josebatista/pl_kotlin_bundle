package io.github.josebatista.chirp.infra.database.repositories

import io.github.josebatista.chirp.infra.database.entity.EmailVerificationTokenEntity
import io.github.josebatista.chirp.infra.database.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)

    @Modifying
    @Query(
        """
        UPDATE EmailVerificationTokenEntity e
        SET e.usedAt = CURRENT_TIMESTAMP 
        WHERE e.user = :user
    """
    )
    fun invalidateActiveTokensForUser(user: UserEntity)
}
