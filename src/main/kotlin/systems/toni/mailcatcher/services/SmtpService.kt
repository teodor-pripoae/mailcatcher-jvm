package systems.toni.mailcatcher.services

import org.apache.commons.mail.util.MimeMessageParser
import org.subethamail.smtp.helper.SimpleMessageListener
import systems.toni.mailcatcher.domain.Mail
import java.io.InputStream
import javax.mail.internet.MimeMessage
import jakarta.inject.Inject
import jakarta.enterprise.context.ApplicationScoped
import io.quarkus.logging.Log

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
        val messageParser = MimeMessageParser(MimeMessage(null, data)).parse()
        return Mail(
            from = messageParser.from,
            to = messageParser.to,
            subject = messageParser.subject,
            textBody = parseTextBody(messageParser),
            htmlBody = parseHtmlBody(messageParser),
        )
    }

    private fun parseTextBody(messageParser: MimeMessageParser) =
        messageParser.plainContent ?: ""

    private fun parseHtmlBody(messageParser: MimeMessageParser) =
        messageParser.htmlContent ?: ""
}
