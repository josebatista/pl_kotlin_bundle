package io.github.josebatista.chirp.infra.database.repository

import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entity.DeviceTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceTokenEntity, Long> {
    fun findByUserIdIn(userIds: List<UserId>): List<DeviceTokenEntity>
    fun findByToken(token: String): DeviceTokenEntity?
    fun deleteByToken(token: String)
}
