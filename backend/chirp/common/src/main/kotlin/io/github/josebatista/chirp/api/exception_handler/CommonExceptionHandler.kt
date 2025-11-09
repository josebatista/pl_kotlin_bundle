package io.github.josebatista.chirp.api.exception_handler

import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_CODE_KEY
import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_MESSAGE_KEY
import io.github.josebatista.chirp.domain.exception.ForbiddenException
import io.github.josebatista.chirp.domain.exception.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CommonExceptionHandler {

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onForbidden(e: ForbiddenException) = mapOf(
        RETURN_CODE_KEY to FORBIDDEN_CODE,
        RETURN_MESSAGE_KEY to e.localizedMessage
    )

    @ExceptionHandler(UnauthorizedException::class)
    fun onUnauthorized(
        e: UnauthorizedException
    ): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                mapOf(
                    RETURN_CODE_KEY to UNAUTHORIZED_ERROR_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    private companion object {
        const val FORBIDDEN_CODE = "FORBIDDEN"
        const val UNAUTHORIZED_ERROR_CODE = "UNAUTHORIZED"
    }
}
