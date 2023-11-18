package systems.toni.mailcatcher.domain

data class MailDto(
	val id: Int,
	val sender: String,
	val recipients: List<String>,
	val subject: String,
    val createdAt: String,
)

object MailDtoMapper {
    fun map(mail: Mail): MailDto {
        return MailDto(
            id = mail.id,
            sender = mail.from,
            recipients = mail.to.map{ it.toString() },
            subject = mail.subject,
            createdAt = mail.receivedAt.toString(),
        )
    }
}