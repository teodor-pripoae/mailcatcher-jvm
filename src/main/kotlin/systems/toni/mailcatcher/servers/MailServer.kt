package systems.toni.mailcatcher.servers

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter
import org.subethamail.smtp.server.SMTPServer
import systems.toni.mailcatcher.services.SmtpService

@ApplicationScoped
class MailServer {
    @Inject
    lateinit var smtpService: SmtpService

    @ConfigProperty(name = "smtp.port", defaultValue = "3025")
    lateinit var smtpPort: String

    fun start() {
        Log.info("Starting mail server...")
        val port = smtpPort.toIntOrNull() ?: throw RuntimeException("Invalid port")
        val smtp =
            SMTPServer.port(port)
                .messageHandlerFactory(SimpleMessageListenerAdapter(smtpService))
                .build()
        smtp.start()
    }
}
