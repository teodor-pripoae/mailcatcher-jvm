package systems.toni.mailcatcher.servers

import org.subethamail.smtp.helper.SimpleMessageListenerAdapter
import org.subethamail.smtp.server.SMTPServer
import org.eclipse.microprofile.config.inject.ConfigProperty
import systems.toni.mailcatcher.services.SmtpService
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.Properties

@ApplicationScoped
class MailServer {
		@Inject
		lateinit var smtpService: SmtpService

		@ConfigProperty(name = "smtp.port", defaultValue = "3025")
		lateinit var smtpPort: String

		fun start() {
				println("Starting mail server...")
				val smtp = SMTPServer(SimpleMessageListenerAdapter(smtpService))
        smtp.port = smtpPort.toIntOrNull() ?: throw RuntimeException("Invalid port")
        smtp.start()
		}
}