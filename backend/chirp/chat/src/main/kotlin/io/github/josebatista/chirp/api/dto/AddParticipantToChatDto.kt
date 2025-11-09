package io.github.josebatista.chirp.api.dto

import io.github.josebatista.chirp.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToChatDto(
    @field:Size(min = 1)
    val userIds: List<UserId>
)
