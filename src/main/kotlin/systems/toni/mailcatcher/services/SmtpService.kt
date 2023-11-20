package systems.toni.mailcatcher.services

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.mail.internet.MimeMessage
import org.simplejavamail.converter.internal.mimemessage.MimeMessageParser
import org.subethamail.smtp.helper.SimpleMessageListener
import systems.toni.mailcatcher.domain.Mail
import java.io.InputStream
import java.time.Instant

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
        Log.error("Parsed mail: $mail")
        storageService.add(mail)
    }

    private fun parseMail(data: InputStream): Mail {
        val source = data.readAllBytes()
        val stream = source.inputStream()
        val sourceString = String(source, Charsets.UTF_8)

        val mimeMessage = MimeMessage(null, stream)
        val messageParser = MimeMessageParser.parseMimeMessage(mimeMessage)
        return Mail(
            from = messageParser.fromAddress?.address ?: "",
            to = messageParser.toAddresses.map { it.address },
            subject = messageParser.subject ?: throw Exception("No subject"),
            textBody = messageParser.plainContent ?: "",
            htmlBody = messageParser.htmlContent ?: "",
            sourceContent = sourceString,
            receivedAt = Instant.now(),
        )
    }
}
