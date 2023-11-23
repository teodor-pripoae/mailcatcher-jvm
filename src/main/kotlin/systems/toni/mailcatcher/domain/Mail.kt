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
) {
    fun contentType(): String {
        if (textBody.isNotEmpty() && htmlBody.isNotEmpty()) {
            return "multipart/alternative"
        } else if (textBody.isNotEmpty()) {
            return "text/plain"
        } else if (htmlBody.isNotEmpty()) {
            return "multipart/mixed"
        } else {
            return "text/plain"
        }
    }

    fun createdAt(): String {
        return receivedAt.toString()
    }

    fun size(): Int {
        return sourceContent.length
    }

    fun formats(): List<String> {
        var formats: List<String> = listOf()
        if (!sourceContent.equals("")) {
            formats = formats.plus("source")
        }

        if (!htmlBody.equals("")) {
            formats = formats.plus("html")
        }
        if (!textBody.equals("")) {
            formats = formats.plus("plain")
        }
        return formats
    }
}