package systems.toni.mailcatcher.services

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.subethamail.smtp.helper.SimpleMessageListener
import systems.toni.mailcatcher.domain.Mail
import systems.toni.mailcatcher.util.MimeMessageParser
import java.io.InputStream
import java.time.Instant
import javax.mail.internet.MimeMessage

@ApplicationScoped
class SmtpService : SimpleMessageListener {
    @Inject
    lateinit var storageService : StorageService

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
        Log.error("Parsed mail: $mail")
        storageService.add(mail)
    }

    private fun parseMail(data: InputStream): Mail {
        val source = data.readAllBytes()
        val stream = source.inputStream()
        val sourceString = String(source, Charsets.UTF_8)

        val messageParser = MimeMessageParser(MimeMessage(null, stream)).parse()
        return Mail(
            from = messageParser.from ?: "",
            to = messageParser.to,
            subject = messageParser.subject,
            textBody = parseTextBody(messageParser),
            htmlBody = parseHtmlBody(messageParser),
            sourceContent = sourceString,
            receivedAt = Instant.now(),
        )
    }

    private fun parseTextBody(messageParser: MimeMessageParser) =
        messageParser.plainContent ?: ""

    private fun parseHtmlBody(messageParser: MimeMessageParser) =
        messageParser.htmlContent ?: ""
}
