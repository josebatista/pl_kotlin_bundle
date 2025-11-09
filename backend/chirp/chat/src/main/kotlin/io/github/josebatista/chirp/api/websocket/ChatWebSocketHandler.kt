package io.github.josebatista.chirp.api.websocket

import io.github.josebatista.chirp.api.dto.ws.ChatParticipantsChangedDto
import io.github.josebatista.chirp.api.dto.ws.DeleteMessageDto
import io.github.josebatista.chirp.api.dto.ws.ErrorDto
import io.github.josebatista.chirp.api.dto.ws.IncomingWebSocketMessage
import io.github.josebatista.chirp.api.dto.ws.IncomingWebSocketMessageType
import io.github.josebatista.chirp.api.dto.ws.OutgoingWebSocketMessage
import io.github.josebatista.chirp.api.dto.ws.OutgoingWebSocketMessageType
import io.github.josebatista.chirp.api.dto.ws.ProfilePictureUpdateDto
import io.github.josebatista.chirp.api.dto.ws.SendMessageDto
import io.github.josebatista.chirp.api.mappers.toChatMessageDto
import io.github.josebatista.chirp.domain.event.ChatParticipantsJoinedEvent
import io.github.josebatista.chirp.domain.event.ChatParticipantsLeftEvent
import io.github.josebatista.chirp.domain.event.MessageDeletedEvent
import io.github.josebatista.chirp.domain.event.ProfilePictureUpdatedEvent
import io.github.josebatista.chirp.domain.type.ChatId
import io.github.josebatista.chirp.domain.type.UserId
import io.github.josebatista.chirp.service.ChatMessageService
import io.github.josebatista.chirp.service.ChatService
import io.github.josebatista.chirp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.DatabindException
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val chatService: ChatService,
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val jwtService: JwtService,
) : TextWebSocketHandler() {

    private companion object {
        const val PING_INTERVAL_MS = 30_000L
        const val PONG_TIMEOUT_MS = 60_000L
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val connectionLock = ReentrantReadWriteLock()
    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>()
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(/* headerName = */ HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing Authorization header.")
                session.close(/* status = */ CloseStatus.SERVER_ERROR.withReason("Authentication failed."))
                return
            }
        val userId = jwtService.getUserIdFromToken(token = authHeader)
        val userSession = UserSession(userId = userId, session = session)
        connectionLock.write {
            sessions[session.id] = userSession
            userToSessions.compute(/* key = */ userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply { add(session.id) }
            }
            val chatId = userChatIds.computeIfAbsent(/* key = */ userId) {
                val chatIds = chatService.findChatsByUser(userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(/* c = */ chatIds)
                }
            }
            chatId.forEach { chatId ->
                chatToSessions.compute(/* key = */ chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }
        logger.info("Websocket connection established for user $userId")
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        connectionLock.write {
            sessions.remove(/* key = */ session.id)?.let { userSession ->
                val userId = userSession.userId
                userToSessions.compute(/* key = */ userId) { _, sessions ->
                    sessions
                        ?.apply { remove(element = session.id) }
                        ?.takeIf { it.isNotEmpty() }
                }
                userChatIds[userId]?.forEach { chatId ->
                    chatToSessions.compute(/* key = */ chatId) { _, sessions ->
                        sessions
                            ?.apply { remove(element = session.id) }
                            ?.takeIf { it.isNotEmpty() }
                    }
                }
                logger.info("Websocket session closed for user: {}", userId)
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        logger.error("Transport error for session ${session.id}", exception)
        session.close(/* status = */ CloseStatus.SERVER_ERROR.withReason(/* reason = */ "Transport error."))
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message {}", message.payload)
        val userSession = connectionLock.read { sessions[session.id] ?: return }
        try {
            val webSocketMessage = objectMapper.readValue(
                /* content = */ message.payload,
                /* valueType = */ IncomingWebSocketMessage::class.java
            )
            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val dto = objectMapper.readValue(
                        /* content = */ webSocketMessage.payload,
                        /* valueType = */ SendMessageDto::class.java
                    )
                    handleSendMessage(
                        dto = dto,
                        senderId = userSession.userId
                    )
                }
            }
        } catch (e: DatabindException) {
            logger.warn("Could not parse message ${message.payload}", e)
            sendError(
                session = userSession.session,
                error = ErrorDto(
                    code = "INVALID_JSON",
                    message = "Incoming JSON or UUID is invalid"
                )
            )
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(event: MessageDeletedEvent) {
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
                payload = objectMapper.writeValueAsString(
                    /* value = */ DeleteMessageDto(
                        chatId = event.chatId,
                        messageId = event.messageId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(event: ChatParticipantsJoinedEvent) {
        connectionLock.write {
            event.userIds.forEach { userId ->
                userChatIds.compute(/* key = */ userId) { _, chatIds ->
                    (chatIds ?: mutableSetOf()).apply { add(element = event.chatId) }
                }
                userToSessions[userId]?.forEach { sessionId ->
                    chatToSessions.compute(/* key = */ event.chatId) { _, sessions ->
                        (sessions ?: mutableSetOf()).apply { add(element = sessionId) }
                    }
                }
            }
        }
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    /* value = */ ChatParticipantsChangedDto(
                        chatId = event.chatId
                    )
                )
            )
        )
    }

    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        connectionLock.write {
            sessions.compute(/* key = */ session.id) { _, userSession ->
                userSession?.copy(
                    lastPongTimestampo = System.currentTimeMillis()
                )
            }
        }
        logger.debug("Received pong from {}", session.id)
    }

    @Scheduled(fixedDelay = PING_INTERVAL_MS)
    fun pingClients() {
        val currentTime = System.currentTimeMillis()
        val sessionsToClose = mutableListOf<String>()
        val sessionSnapshot = connectionLock.read { sessions.toMap() }
        sessionSnapshot.forEach { (sessionId, userSession) ->
            try {
                if (userSession.session.isOpen) {
                    val lastPong = userSession.lastPongTimestampo
                    if ((currentTime - lastPong) > PONG_TIMEOUT_MS) {
                        logger.warn("Session $sessionId has timed out, closing connection.")
                        sessionsToClose.add(element = sessionId)
                        return@forEach
                    }
                    userSession.session.sendMessage(PingMessage())
                    logger.debug("Sent ping to {}.", userSession.userId)
                }
            } catch (e: Exception) {
                logger.error("Could not ping session $sessionId", e)
                sessionsToClose.add(element = sessionId)
            }
        }
        sessionsToClose.forEach { sessionId ->
            connectionLock.read {
                sessions[sessionId]?.session?.let { session ->
                    try {
                        session.close(/* status = */ CloseStatus.GOING_AWAY.withReason(/* reason = */ "Ping Timeout."))
                    } catch (e: Exception) {
                        logger.error("Couldn't close sessions for session ${session.id}.", e)
                    }
                }
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeftChat(
        event: ChatParticipantsLeftEvent
    ) {
        connectionLock.write {
            userChatIds.compute(/* key = */ event.userId) { _, chatIds ->
                chatIds
                    ?.apply { remove(element = event.chatId) }
                    ?.takeIf { it.isNotEmpty() }
            }
            userToSessions[event.userId]?.forEach { sessionId ->
                chatToSessions.compute(/* key = */ event.chatId) { _, sessions ->
                    sessions
                        ?.apply { remove(element = sessionId) }
                        ?.takeIf { it.isNotEmpty() }
                }
            }
        }
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    /* value = */ ChatParticipantsChangedDto(
                        chatId = event.chatId
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onProfilePictureUpdated(event: ProfilePictureUpdatedEvent) {
        val userChats = connectionLock.read {
            userChatIds[event.userId]?.toList() ?: emptyList()
        }
        val dto = ProfilePictureUpdateDto(
            userId = event.userId,
            newUrl = event.newUrl
        )
        val sessionIds = mutableSetOf<String>()
        userChats.forEach { chatId ->
            connectionLock.read {
                chatToSessions[chatId]?.let { sessions ->
                    sessionIds.addAll(elements = sessions)
                }
            }
        }
        val webSocketMessage = OutgoingWebSocketMessage(
            type = OutgoingWebSocketMessageType.PROFILE_PICTURE_UPDATED,
            payload = objectMapper.writeValueAsString(/* value = */ dto)
        )
        val messageJson = objectMapper.writeValueAsString(/* value = */ webSocketMessage)
        sessionIds.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId]
            } ?: return@forEach
            try {
                if (userSession.session.isOpen) {
                    userSession.session.sendMessage(/* message = */ TextMessage(/* payload = */ messageJson))
                }
            } catch (e: Exception) {
                logger.error("Could not send profile picture update to session $sessionId", e)
            }
        }
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val webSocketMessage = objectMapper.writeValueAsString(
            /* value = */ OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = objectMapper.writeValueAsString(/* value = */ error)
            )
        )
        try {
            session.sendMessage(TextMessage(/* payload = */ webSocketMessage))
        } catch (e: Exception) {
            logger.warn("Couldn't send error message", e)
        }
    }

    private fun sendToUser(userId: UserId, message: OutgoingWebSocketMessage) {
        val userSession = connectionLock.read { userToSessions[userId] ?: emptySet() }
        userSession.forEach { sessionId ->
            val userSession = connectionLock.read { sessions[sessionId] ?: return@forEach }
            if (userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(/* value = */ message)
                    userSession.session.sendMessage(TextMessage(/* payload = */ messageJson))
                    logger.debug("Sent message to user {}: {}", userId, messageJson)
                } catch (e: Exception) {
                    logger.error("Error while sending message to $userId", e)
                }
            }
        }
    }

    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage
    ) {
        val chatSession = connectionLock.read { chatToSessions[chatId] ?: emptyList() }
        chatSession.forEach { sessionId ->
            val userSession = connectionLock.read { sessions[sessionId] ?: return@forEach }
            sendToUser(
                userId = userSession.userId,
                message = message
            )
        }
    }

    private fun handleSendMessage(
        dto: SendMessageDto,
        senderId: UserId,
    ) {
        val userChatIds = connectionLock.read { userChatIds[senderId] ?: return }
        if (dto.chatId !in userChatIds) return
        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )
        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(/* value = */ savedMessage.toChatMessageDto())
            )
        )
    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
        val lastPongTimestampo: Long = System.currentTimeMillis()
    )
}
