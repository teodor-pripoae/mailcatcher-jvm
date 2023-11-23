package systems.toni.mailcatcher.domain

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection
data class NewMessage(
    val id : Int,
    val sender: String,
    val recipients: List<String>,
    val size: String,
    val subject: String,
    val type: String,
    @get:JsonProperty("created_at")
    val createdAt: String,
) {

    companion object {
        fun fromMail(mail: Mail): NewMessage {
            return NewMessage(
                id = mail.id,
                sender = "<${mail.from}>",
                recipients = mail.to.map{ "<$it>" },
                size = mail.size().toString(),
                subject = mail.subject,
                type = mail.contentType(),
                createdAt = mail.createdAt()
            )
        }
    }
}

@RegisterForReflection
data class WebsocketNewMessage(
    @JsonProperty("type")
    val type: String = "add",
    @JsonProperty("message")
    var message: NewMessage
) {
    companion object {
        fun fromMail(mail: Mail): WebsocketNewMessage {
            return WebsocketNewMessage(
                message = NewMessage.fromMail(mail)
            )
        }
    }
}

@RegisterForReflection
data class WebsocketClearMessage(
    val type: String = "clear"
)