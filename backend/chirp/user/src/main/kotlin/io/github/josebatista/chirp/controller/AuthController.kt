package io.github.josebatista.chirp.controller

import io.github.josebatista.chirp.api.dto.RegisterRequest
import io.github.josebatista.chirp.api.dto.UserDto
import io.github.josebatista.chirp.api.mappers.toUserDto
import io.github.josebatista.chirp.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
    }
}
