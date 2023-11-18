package systems.toni.mailcatcher.services

import systems.toni.mailcatcher.domain.Mail
import jakarta.enterprise.context.ApplicationScoped
import java.util.concurrent.atomic.AtomicInteger

@ApplicationScoped
class StorageService {
	private val mails = mutableListOf<Mail>()
	private val mailsById = mutableMapOf<String, Mail>()
	private var lastId: AtomicInteger = AtomicInteger(0)

	fun add(mail: Mail) {
		if (mail.id == 0) {
			mail.id = lastId.incrementAndGet()
		}
		mailsById[mail.id.toString()] = mail
		mails.add(mail)
	}

	fun getMails() = mails.toList()

	fun getMailById(id: String) = mailsById[id]

	fun deleteAll() {
		mails.clear()
		mailsById.clear()
	}
}