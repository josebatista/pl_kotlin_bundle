package io.github.josebatista.chirp.service

import io.github.josebatista.chirp.domain.exception.EncodePasswordException
import io.github.josebatista.chirp.domain.exception.UserAlreadyExistsException
import io.github.josebatista.chirp.domain.model.User
import io.github.josebatista.chirp.infra.database.entity.UserEntity
import io.github.josebatista.chirp.infra.database.mappers.toUser
import io.github.josebatista.chirp.infra.database.repositories.UserRepository
import io.github.josebatista.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
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
}
