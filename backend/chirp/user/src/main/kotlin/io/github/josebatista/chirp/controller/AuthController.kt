package io.github.josebatista.chirp.controller

import io.github.josebatista.chirp.api.config.IpRateLimit
import io.github.josebatista.chirp.api.dto.AuthenticatedUserDto
import io.github.josebatista.chirp.api.dto.ChangePasswordRequest
import io.github.josebatista.chirp.api.dto.EmailRequest
import io.github.josebatista.chirp.api.dto.LoginRequest
import io.github.josebatista.chirp.api.dto.RefreshRequest
import io.github.josebatista.chirp.api.dto.RegisterRequest
import io.github.josebatista.chirp.api.dto.ResetPasswordRequest
import io.github.josebatista.chirp.api.dto.UserDto
import io.github.josebatista.chirp.api.mappers.toAuthenticatedUserDto
import io.github.josebatista.chirp.api.mappers.toUserDto
import io.github.josebatista.chirp.api.util.requestUserId
import io.github.josebatista.chirp.infra.rate_limiting.EmailRateLimiter
import io.github.josebatista.chirp.service.AuthService
import io.github.josebatista.chirp.service.EmailVerificationTokenService
import io.github.josebatista.chirp.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationTokenService: EmailVerificationTokenService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter,
) {

    @PostMapping("/register")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
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
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun login(
        @Valid @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
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

    @PostMapping("/resend-verification")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest
    ) {
        emailRateLimiter.withRateLimit(email = body.email) {
            emailVerificationTokenService.resendVerificationEmail(email = body.email)
        }
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationTokenService.verifyEmail(token = token)
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun forgotEmail(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(body.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetToken(token = body.token, newPassword = body.newPassword)
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest
    ) {
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword
        )
    }
}
