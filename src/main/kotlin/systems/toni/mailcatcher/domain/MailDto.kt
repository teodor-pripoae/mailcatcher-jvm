package systems.toni.mailcatcher.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class MailDto(
    val id: Int,
    val sender: String,
    val attachments: List<String> = listOf(),
    val recipients: List<String>,
    val subject: String,
    @JsonProperty("created_at")
    val createdAt: String,
    val formats: List<String>,
    val type: String
)

object MailDtoMapper {
    fun map(mail: Mail): MailDto {
        var formats: List<String> = listOf()
        if (!mail.sourceContent.equals("")) {
            formats = formats.plus("source")
        }

        if (!mail.htmlBody.equals("")) {
            formats = formats.plus("html")
        }
        if (!mail.textBody.equals("")) {
            formats = formats.plus("plain")
        }

        return MailDto(
            id = mail.id,
            sender = mail.from,
            recipients = mail.to.map { it.toString() },
            subject = mail.subject,
            createdAt = mail.receivedAt.toString(),
            formats = formats,
            type = "text/alternative"
        )
    }
}