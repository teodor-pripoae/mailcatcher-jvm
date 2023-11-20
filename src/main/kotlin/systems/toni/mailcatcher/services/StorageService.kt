package systems.toni.mailcatcher.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import systems.toni.mailcatcher.domain.Mail
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
class StorageService {
    private val mails = mutableListOf<Mail>()
    private val mailsById = mutableMapOf<String, Mail>()
    private var lastId: AtomicInteger = AtomicInteger(0)

    @Inject
    private lateinit var sessionsService: WebsocketSessionService

    fun add(mail: Mail) {
        if (mail.id == 0) {
            mail.id = lastId.incrementAndGet()
        }
        mailsById[mail.id.toString()] = mail
        mails.add(mail)
        sessionsService.newMail(mail)
    }

    fun getMails() = mails.toList()

    fun getMailById(id: String) = mailsById[id]

    fun deleteAll() {
        mails.clear()
        mailsById.clear()
    }

    fun deleteById(id: String): Mail? {
        val mail = mailsById[id]
        if (mail != null) {
            mails.remove(mail)
            mailsById.remove(id)
            return mail
        }
        return null
    }
}