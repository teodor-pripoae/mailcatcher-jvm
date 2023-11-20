package systems.toni.mailcatcher.domain

data class WebsocketMessage(
    val type: String,
    val message: MailDto
)