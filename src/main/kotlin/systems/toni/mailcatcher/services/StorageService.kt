package systems.toni.mailcatcher.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import systems.toni.mailcatcher.domain.Mail
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
class StorageService {
    private val mails = mutableListOf<Mail>()
    private val mailsById = mutableMapOf<String, Mail>()
    private val mailsByMessageid = mutableMapOf<String, Mail>()
    private var lastId: AtomicInteger = AtomicInteger(0)

    @Inject
    private lateinit var sessionsService: WebsocketSessionService

    fun add(mail: Mail) {
        synchronized(this) {
            // if there is a mail sent to multiple recipients
            // we will receive it multiple times from subethasmtp
            if (mailsByMessageid.containsKey(mail.messageId)) {
                return
            }

            if (mail.id == 0) {
                mail.id = lastId.incrementAndGet()
            }
            mailsById[mail.id.toString()] = mail
            mailsByMessageid[mail.messageId] = mail
            mails.add(mail)
            sessionsService.newMail(mail)
        }
    }

    fun getMails() = synchronized(this) { mails.toList() }

    fun getMailById(id: String) = synchronized(this) { mailsById[id] }

    fun deleteAll() {
        synchronized(this) {
            mails.clear()
            mailsById.clear()
            mailsByMessageid.clear()
            sessionsService.clear()
            lastId.set(0)
        }
    }

    fun deleteById(id: String): Mail? {
        synchronized(this) {
            val mail = mailsById[id]
            if (mail != null) {
                mails.remove(mail)
                mailsById.remove(id)
                mailsByMessageid.remove(mail.messageId)
                return mail
            }
            return null
        }
    }
}