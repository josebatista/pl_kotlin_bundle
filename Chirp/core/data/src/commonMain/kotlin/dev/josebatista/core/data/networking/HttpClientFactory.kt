package dev.josebatista.core.data.networking

import dev.josebatista.core.data.BuildKonfig
import dev.josebatista.core.data.dto.AuthInfoSerializable
import dev.josebatista.core.data.dto.requests.RefreshRequest
import dev.josebatista.core.data.mappers.toDomain
import dev.josebatista.core.domain.auth.SessionStorage
import dev.josebatista.core.domain.logging.ChirpLogger
import dev.josebatista.core.domain.util.onFailure
import dev.josebatista.core.domain.util.onSuccess
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val chirpLogger: ChirpLogger,
    private val sessionStorage: SessionStorage
) {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine = engine) {
            install(plugin = ContentNegotiation) {
                json(json = Json { ignoreUnknownKeys = true })
            }
            install(plugin = HttpTimeout) {
                socketTimeoutMillis = TIMEOUT_VALUE
                requestTimeoutMillis = TIMEOUT_VALUE
            }
            install(plugin = Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        chirpLogger.debug(message = message)
                    }
                }
                level = LogLevel.ALL
            }
            install(plugin = WebSockets) {
                pingIntervalMillis = TIMEOUT_VALUE
            }
            defaultRequest {
                header("x-api-key", BuildKonfig.API_KEY)
                contentType(type = ContentType.Application.Json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        sessionStorage
                            .observeAuthInfo()
                            .firstOrNull()
                            ?.let {
                                BearerTokens(
                                    accessToken = it.accessToken,
                                    refreshToken = it.refreshToken
                                )
                            }
                    }
                    refreshTokens {
                        if (response.request.url.encodedPath.contains("auth/")) {
                            return@refreshTokens null
                        }
                        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                        if (authInfo?.refreshToken.isNullOrBlank()) {
                            sessionStorage.set(null)
                            return@refreshTokens null
                        }
                        var bearerToken: BearerTokens? = null
                        client.post<RefreshRequest, AuthInfoSerializable>(
                            route = "/auth/refresh",
                            body = RefreshRequest(refreshToken = authInfo.refreshToken),
                            builder = {
                                markAsRefreshTokenRequest()
                            }
                        ).onSuccess { newAuthInfo ->
                            val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
                            authInfo?.let {
                                sessionStorage.set(
                                    authInfo.copy(
                                        accessToken = newAuthInfo.accessToken,
                                        refreshToken = newAuthInfo.refreshToken
                                    )
                                )
                            } ?: run {
                                sessionStorage.set(newAuthInfo.toDomain())
                            }
                            bearerToken = BearerTokens(
                                accessToken = newAuthInfo.accessToken,
                                refreshToken = newAuthInfo.refreshToken
                            )
                        }.onFailure {
                            sessionStorage.set(null)
                        }
                        bearerToken
                    }
                }
            }
        }
    }

    private companion object {
        const val TIMEOUT_VALUE = 20_000L
    }
}
