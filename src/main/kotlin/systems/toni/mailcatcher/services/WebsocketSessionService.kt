package systems.toni.mailcatcher.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.websocket.Session
import systems.toni.mailcatcher.domain.Mail
import systems.toni.mailcatcher.domain.MailDtoMapper
import systems.toni.mailcatcher.domain.WebsocketMessage
import java.util.concurrent.ConcurrentHashMap

@ApplicationScoped
class WebsocketSessionService {
    private var sessions: MutableMap<String, Session> = ConcurrentHashMap<String, Session>()
    private var objectMapper = ObjectMapper()

    fun add(session: Session?) {
        if (session != null) {
            sessions.put(session.id, session)
        } else {
            throw IllegalArgumentException("Session must not be null")
        }
    }

    fun remove(session: Session?) {
        if (session != null) {
            sessions.remove(session.id)
        } else {
            throw IllegalArgumentException("Session must not be null")
        }
    }

    fun newMail(mail: Mail) {
        val message = MailDtoMapper.map(mail)
        val response = WebsocketMessage("add", message)
        val json = objectMapper.writeValueAsString(response)
        broadcast(json)
    }

    fun broadcast(message: String) {
        sessions.values.forEach { session ->
            session.asyncRemote.sendObject(message) { result ->
                if (result.exception != null) {
                    println("Unable to send message: ${result.exception}")
                }
            }
        }
    }
}