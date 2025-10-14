package io.github.josebatista.chirp.controller

import io.github.josebatista.chirp.api.dto.AuthenticatedUserDto
import io.github.josebatista.chirp.api.dto.LoginRequest
import io.github.josebatista.chirp.api.dto.RefreshRequest
import io.github.josebatista.chirp.api.dto.RegisterRequest
import io.github.josebatista.chirp.api.dto.UserDto
import io.github.josebatista.chirp.api.mappers.toAuthenticatedUserDto
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

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService.refreshToken(refreshToken = body.refreshToken).toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody body: RefreshRequest
    ) {
        authService.logout(refreshToken = body.refreshToken)
    }
}
