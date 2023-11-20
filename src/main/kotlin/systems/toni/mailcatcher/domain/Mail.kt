package systems.toni.mailcatcher.domain

import java.time.Instant

data class Mail(
    var id: Int = 0,
    val from: String,
    val to: List<String>,
    val subject: String,
    val textBody: String,
    val htmlBody: String,
    val sourceContent: String,
    val receivedAt: Instant
)