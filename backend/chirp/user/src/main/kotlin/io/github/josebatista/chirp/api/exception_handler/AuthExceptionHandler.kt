package io.github.josebatista.chirp.api.exception_handler

import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_CODE_KEY
import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_MESSAGE_KEY
import io.github.josebatista.chirp.domain.exception.EmailNotVerifiedException
import io.github.josebatista.chirp.domain.exception.EncodePasswordException
import io.github.josebatista.chirp.domain.exception.InvalidCredentialsException
import io.github.josebatista.chirp.domain.exception.InvalidTokenException
import io.github.josebatista.chirp.domain.exception.RateLimitException
import io.github.josebatista.chirp.domain.exception.SamePasswordException
import io.github.josebatista.chirp.domain.exception.UserAlreadyExistsException
import io.github.josebatista.chirp.domain.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun onUserAlreadyExists(e: UserAlreadyExistsException): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                mapOf(
                    RETURN_CODE_KEY to USER_EXISTS_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun onUserNotFound(
        e: UserNotFoundException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                mapOf(
                    RETURN_CODE_KEY to USER_NOT_FOUND_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(
        e: MethodArgumentNotValidException
    ): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map { it.defaultMessage ?: DEFAULT_ERROR_MESSAGE }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    RETURN_CODE_KEY to VALIDATION_ERROR_CODE,
                    RETURN_MESSAGE_KEY to errors
                )
            )
    }

    @ExceptionHandler(EncodePasswordException::class)
    fun onEncodePasswordException(
        e: EncodePasswordException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    RETURN_CODE_KEY to VALIDATION_ERROR_CODE,
                    RETURN_MESSAGE_KEY to "Verify your password"
                )
            )
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun onInvalidTokenException(
        e: InvalidTokenException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                mapOf(
                    RETURN_CODE_KEY to INVALID_TOKEN_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun onInvalidCredentials(
        e: InvalidCredentialsException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                mapOf(
                    RETURN_CODE_KEY to INVALID_CREDENTIALS_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    @ExceptionHandler(EmailNotVerifiedException::class)
    fun onEmailNotVerified(
        e: EmailNotVerifiedException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                mapOf(
                    RETURN_CODE_KEY to EMAIL_NOT_VERIFIED_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    @ExceptionHandler(SamePasswordException::class)
    fun onSamePassword(
        e: SamePasswordException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                mapOf(
                    RETURN_CODE_KEY to SAME_PASSWORD_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    @ExceptionHandler(RateLimitException::class)
    fun onRateLimit(
        e: RateLimitException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(
                mapOf(
                    RETURN_CODE_KEY to RATE_LIMIT_EXCEEDED_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    private companion object {
        const val DEFAULT_ERROR_MESSAGE = "Invalid value"

        const val USER_EXISTS_CODE = "USER_EXISTS"
        const val USER_NOT_FOUND_CODE = "USER_NOT_FOUND"
        const val VALIDATION_ERROR_CODE = "VALIDATION_ERROR"
        const val INVALID_TOKEN_CODE = "INVALID_TOKEN"
        const val INVALID_CREDENTIALS_CODE = "INVALID_CREDENTIALS"
        const val EMAIL_NOT_VERIFIED_CODE = "EMAIL_NOT_VERIFIED"
        const val SAME_PASSWORD_CODE = "SAME_PASSWORD"
        const val RATE_LIMIT_EXCEEDED_CODE = "RATE_LIMIT_EXCEEDED"
    }
}
