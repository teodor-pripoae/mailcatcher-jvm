package systems.toni.mailcatcher.api

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import jakarta.websocket.server.ServerEndpoint
import jakarta.ws.rs.PathParam
import systems.toni.mailcatcher.services.WebsocketSessionService
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

@ServerEndpoint("/messages")
@ApplicationScoped
class ChatSocket {
    @Inject
    private lateinit var sessionsService: WebsocketSessionService

    @OnOpen
    fun onOpen(session: Session) {
        Log.info("Websocket opened: ${session.id}")
        sessionsService.add(session)
    }

    @OnClose
    fun onClose(session: Session?) {
        Log.info("Websocket closed: ${session?.id}")
        sessionsService.remove(session)
    }

    @OnError
    fun onError(session: Session?, throwable: Throwable) {
        Log.info("Websocket error: $throwable")
        sessionsService.remove(session)
    }

    @OnMessage
    fun onMessage(message: String) {
        Log.info("Websocket message: $message")
    }
}