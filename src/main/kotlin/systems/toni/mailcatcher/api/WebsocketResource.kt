package systems.toni.mailcatcher.api

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.websocket.*
import jakarta.websocket.server.ServerEndpoint
import systems.toni.mailcatcher.services.WebsocketSessionService

@ServerEndpoint("/messages")
@ApplicationScoped
class WebsocketResource {
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