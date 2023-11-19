package systems.toni.mailcatcher.domain

import java.time.Instant
import javax.mail.Address

data class Mail(
    var id: Int = 0,
    val from: String,
    val to: List<Address>,
    val subject: String,
    val textBody: String,
    val htmlBody: String,
    val sourceContent: String,
    val receivedAt: Instant
)