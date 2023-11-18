package systems.toni.mailcatcher.domain

data class MailDto(
	val id: Int,
	val sender: String,
	val recipients: List<String>,
	val subject: String,
)