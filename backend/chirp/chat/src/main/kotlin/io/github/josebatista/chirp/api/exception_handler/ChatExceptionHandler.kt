package io.github.josebatista.chirp.api.exception_handler

import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_CODE_KEY
import io.github.josebatista.chirp.api.exception_handler.util.CommonExceptionKeys.RETURN_MESSAGE_KEY
import io.github.josebatista.chirp.domain.exception.ChatNotFoundException
import io.github.josebatista.chirp.domain.exception.ChatParticipantNotFoundException
import io.github.josebatista.chirp.domain.exception.InvalidChatSizeException
import io.github.josebatista.chirp.domain.exception.InvalidProfilePictureException
import io.github.josebatista.chirp.domain.exception.MessageNotFoundException
import io.github.josebatista.chirp.domain.exception.StorageException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        ChatParticipantNotFoundException::class,
        MessageNotFoundException::class
    )
    fun onNotFound(e: Exception): ResponseEntity<Map<String, Any>> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                mapOf(
                    RETURN_CODE_KEY to NOT_FOUND_CODE,
                    RETURN_MESSAGE_KEY to e.localizedMessage
                )
            )
    }

    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidChatSize(e: InvalidChatSizeException) = mapOf(
        RETURN_CODE_KEY to INVALID_CHAT_SIZE_CODE,
        RETURN_MESSAGE_KEY to e.localizedMessage
    )

    @ExceptionHandler(InvalidProfilePictureException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidProfilePicture(e: InvalidProfilePictureException) = mapOf(
        RETURN_CODE_KEY to INVALID_PROFILE_PICTURE_CODE,
        RETURN_MESSAGE_KEY to e.localizedMessage
    )

    @ExceptionHandler(StorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onStorageError(e: StorageException) = mapOf(
        RETURN_CODE_KEY to STORAGE_ERROR_CODE,
        RETURN_MESSAGE_KEY to e.localizedMessage
    )

    private companion object {
        const val NOT_FOUND_CODE = "NOT_FOUND"
        const val INVALID_CHAT_SIZE_CODE = "INVALID_CHAT_SIZE"
        const val INVALID_PROFILE_PICTURE_CODE = "INVALID_PROFILE_PICTURE"
        const val STORAGE_ERROR_CODE = "STORAGE_ERROR"
    }
}
