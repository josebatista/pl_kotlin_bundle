package io.github.josebatista.chirp.infra.database.repository

import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entities.ChatParticipantEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatParticipantRepository : JpaRepository<ChatParticipantEntity, UserId> {
    fun findByUserIdIn(userIds: Set<UserId>): Set<ChatParticipantEntity>

    @Query(
        """
        SELECT p
        FROM ChatParticipantEntity p
        WHERE LOWER(p.username) = :query OR LOWER(p.email) = :query
    """
    )
    fun findByEmailOrUsername(query: String): ChatParticipantEntity?
}
