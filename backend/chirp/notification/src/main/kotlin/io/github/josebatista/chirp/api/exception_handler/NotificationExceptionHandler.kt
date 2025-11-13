package io.github.josebatista.chirp.api.exception_handler

import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_CODE_KEY
import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_MESSAGE_KEY
import io.github.josebatista.chirp.domain.expection.InvalidDeviceTokenException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NotificationExceptionHandler {

    @ExceptionHandler(InvalidDeviceTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidDeviceToken(e: InvalidDeviceTokenException) = mapOf(
        RETURN_CODE_KEY to INVALID_DEVICE_TOKEN_CODE,
        RETURN_MESSAGE_KEY to e.localizedMessage
    )

    private companion object {
        const val INVALID_DEVICE_TOKEN_CODE = "INVALID_DEVICE_TOKEN"
    }
}
