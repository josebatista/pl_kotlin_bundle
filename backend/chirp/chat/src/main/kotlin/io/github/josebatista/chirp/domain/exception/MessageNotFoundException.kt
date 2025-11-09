package io.github.josebatista.chirp.domain.exception

import io.github.josebatista.chirp.domain.type.ChatMessageId

class MessageNotFoundException(id: ChatMessageId) : RuntimeException("Message with ID $id not found.")
