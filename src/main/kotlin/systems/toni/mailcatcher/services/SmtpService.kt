package systems.toni.mailcatcher.services

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.mail.internet.MimeMessage
import org.simplejavamail.converter.internal.mimemessage.MimeMessageParser
import org.subethamail.smtp.helper.SimpleMessageListener
import systems.toni.mailcatcher.domain.Address
import systems.toni.mailcatcher.domain.Attachment
import systems.toni.mailcatcher.domain.Mail
import java.io.InputStream
import java.time.Instant
import java.util.UUID

@ApplicationScoped
class SmtpService : SimpleMessageListener {
    @Inject
    lateinit var storageService: StorageService

    override fun accept(
        from: String,
        recipient: String,
    ) = true

    override fun deliver(
        from: String,
        recipient: String,
        data: InputStream,
    ) {
        Log.info("Received mail from $from to $recipient")
        val mail = parseMail(data)
        Log.info("Parsed mail:\n$mail")
        storageService.add(mail)
    }

    private fun parseMail(data: InputStream): Mail {
        val source = data.readAllBytes()
        val stream = source.inputStream()
        val sourceString = String(source, Charsets.UTF_8)

        val mimeMessage = MimeMessage(null, stream)
        val messageParser = MimeMessageParser.parseMimeMessage(mimeMessage)

        val contentType = mimeMessage.contentType ?: "text/plain"

        val mail = Mail(
            from = messageParser.fromAddress?.address ?: "",
            to = messageParser.toAddresses.map { Address(it) },
            subject = messageParser.subject ?: throw Exception("No subject"),
            textBody = messageParser.plainContent ?: "",
            htmlBody = messageParser.htmlContent ?: "",
            receivedAt = Instant.now(),
            attachments = messageParser.attachmentList.map {
                Attachment.fromMimeDataSource(it)
            },
            messageId = messageParser.messageId ?: UUID.randomUUID().toString(),
            contentType = contentType.split(";")[0],
        )
        mail.sourceContent = transformSourceContent(sourceString)
        return mail
    }

    private fun transformSourceContent(sourceContent: String): String {
        // hack, it seems subetha smtp adds a line "Received: from ..." to the beginning of the message
        // and then some lines starting with spaces
        // so we just remove that lines
        val lines = sourceContent.lines()
        if (!lines.first().startsWith("Received: from")) {
            return sourceContent
        }
        var i = 1
        while (i < lines.size && lines[i].startsWith(" ")) {
            i++
        }

        val goodLines = lines.subList(i, lines.size)
        val text = goodLines.joinToString("\r\n")
        return text
    }
}
