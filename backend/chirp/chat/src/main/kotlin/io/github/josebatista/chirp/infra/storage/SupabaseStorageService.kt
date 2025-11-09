package io.github.josebatista.chirp.infra.storage

import io.github.josebatista.chirp.domain.exception.InvalidProfilePictureException
import io.github.josebatista.chirp.domain.exception.StorageException
import io.github.josebatista.chirp.domain.models.ProfilePictureUploadCredentials
import io.github.josebatista.chirp.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.UUID

@Service
class SupabaseStorageService(
    @param:Value($$"${supabase.url}") private val supabaseUrl: String,
    private val supabaseRestClient: RestClient
) {

    private companion object {
        const val DEFAULT_EXPIRATION_SECONDS = 300
        const val SUPABASE_BUCKET_NAME = "profile-pictures"
        val allowedMimeTypes = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp",
        )
    }

    fun generateSignedUploadUrl(userId: UserId, mimeType: String): ProfilePictureUploadCredentials {
        val extension = allowedMimeTypes[mimeType]
            ?: throw InvalidProfilePictureException(message = "Invalid mime type $mimeType")
        val fileName = "user_${userId}_${UUID.randomUUID()}.$extension"
        val path = "$SUPABASE_BUCKET_NAME/$fileName"
        val publicUrl = "$supabaseUrl/storage/v1/object/public/$path"
        return ProfilePictureUploadCredentials(
            uploadUrl = createSignedUrl(path = path, expiresInSeconds = DEFAULT_EXPIRATION_SECONDS),
            publicUrl = publicUrl,
            headers = mapOf(
                HttpHeaders.CONTENT_TYPE to mimeType
            ),
            expiresAt = Instant.now().plusSeconds(/* secondsToAdd = */ DEFAULT_EXPIRATION_SECONDS.toLong())
        )
    }

    fun deleteFile(url: String) {
        val path = if (url.contains(other = "/object/public/")) {
            url.substringAfter(delimiter = "/object/public/")
        } else throw StorageException(message = "Invalid file URL format")
        val deleteUrl = "/storage/v1/object/$path"
        val response = supabaseRestClient
            .delete()
            .uri(/* uri = */ deleteUrl)
            .retrieve()
            .toBodilessEntity()

        if (response.statusCode.isError) throw StorageException(message = "Unable to delete file: ${response.statusCode.value()}")
    }

    private fun createSignedUrl(path: String, expiresInSeconds: Int): String {
        val json = """
            { "expiresIn": $expiresInSeconds }
        """.trimIndent()
        val response = supabaseRestClient.post()
            .uri(/* uri = */ "/storage/v1/object/upload/sign/$path")
            .header( /* headerName = */ HttpHeaders.CONTENT_TYPE,
                /* ...headerValues = */ MediaType.APPLICATION_JSON_VALUE
            )
            .body(/* body = */ json)
            .retrieve()
            .body(/* bodyType = */ SignedUploadResponse::class.java)
            ?: throw StorageException(message = "Failed to create a signed URL")
        return "$supabaseUrl/storage/v1${response.url}"
    }

    private data class SignedUploadResponse(
        val url: String
    )
}
