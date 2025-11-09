package io.github.josebatista.chirp.api.mappers

import io.github.josebatista.chirp.api.dto.PictureUploadResponse
import io.github.josebatista.chirp.domain.models.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse(): PictureUploadResponse = PictureUploadResponse(
    uploadUrl = uploadUrl,
    publicUrl = publicUrl,
    headers = headers,
    expiresAt = expiresAt
)
