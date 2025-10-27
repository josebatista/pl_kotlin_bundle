package io.github.josebatista.chirp.infra.database.repositories

import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.infra.database.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?
}
