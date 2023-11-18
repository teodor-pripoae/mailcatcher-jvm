package systems.toni.mailcatcher.util

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import io.quarkus.runtime.StartupEvent
import systems.toni.mailcatcher.servers.MailServer

@ApplicationScoped
class ApplicationLifecycle {
    @Inject
    lateinit var mailServer: MailServer

    fun onStart(@Observes event: StartupEvent) {
        Thread {
            mailServer.start()
        }.start()
    }

    fun onStop() {
        // stop your background thread here
    }
}
