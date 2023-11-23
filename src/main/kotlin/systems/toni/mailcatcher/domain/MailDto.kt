package systems.toni.mailcatcher.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class AttachmentDto(
    val filename: String,
    val type: String,
    val size: Int,
) {
    companion object {
        fun fromAttachment(attachment: Attachment): AttachmentDto {
            return AttachmentDto(
                filename = attachment.filename,
                type = attachment.type,
                size = attachment.size,
            )
        }
    }
}

data class MailDto(
    val id: Int,
    val sender: String,
    val attachments: List<AttachmentDto>,
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
            // this is a strange bug from the original mailcatcher
            // if there is a mail sent to multiple recipients
            // then the recipients list will contain the email address formatted with name (if provided)
            // if there is only one recipient, then the email address will be formatted without name
            val recipients = if (mail.to.size == 1) {
                mail.to.map { it.emailFormatted() }
            } else {
                mail.to.map { it.toString() }
            }

            return MailDto(
                id = mail.id,
                sender = "<${mail.from}>",
                recipients = recipients,
                subject = mail.subject,
                createdAt = mail.receivedAt.toString(),
                size = mail.size().toString(),
                formats = mail.formats(),
                type = mail.contentType,
                attachments = mail.attachments.map { AttachmentDto.fromAttachment(it) }
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
            // this is a strange bug from the original mailcatcher
            // if there is a mail sent to multiple recipients
            // then the recipients list will contain the email address formatted with name (if provided)
            // if there is only one recipient, then the email address will be formatted without name
            val recipients = if (mail.to.size == 1) {
                mail.to.map { it.emailFormatted() }
            } else {
                mail.to.map { it.toString() }
            }

            return MailSimpleDto(
                id = mail.id,
                sender = "<${mail.from}>",
                recipients = recipients,
                subject = mail.subject,
                size = mail.size().toString(),
                createdAt = mail.createdAt()
            )
        }
    }
}