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
    val type: String,
    val size: String,
) {
    companion object {
        fun fromMail(mail: Mail): MailDto {
            return MailDto(
                id = mail.id,
                sender = "<${mail.from}>",
                recipients = mail.to.map { "<$it>" },
                subject = mail.subject,
                createdAt = mail.receivedAt.toString(),
                size = mail.size().toString(),
                formats = mail.formats(),
                type = mail.contentType()
            )
        }
    }
}

data class MailSimpleDto(
    val id: Int,
    val sender: String,
    val recipients: List<String>,
    val subject: String,
    val size: String,
    @JsonProperty("created_at")
    val createdAt: String,
) {
    companion object {
        fun fromMail(mail: Mail): MailSimpleDto {
            return MailSimpleDto(
                id = mail.id,
                sender = "<${mail.from}>",
                recipients = mail.to.map { "<$it>" },
                subject = mail.subject,
                size = mail.size().toString(),
                createdAt = mail.createdAt()
            )
        }
    }
}