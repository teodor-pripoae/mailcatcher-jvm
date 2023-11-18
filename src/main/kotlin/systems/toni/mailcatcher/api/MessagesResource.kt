package systems.toni.mailcatcher.api

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.inject.Inject
import systems.toni.mailcatcher.domain.MailDto
import systems.toni.mailcatcher.services.StorageService

@Path("/messages")
class MessagesResource {
    @Inject
    lateinit var storageService: StorageService

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun messages(): List<MailDto> {
        val mails = storageService.getMails()
        val mailDtos = mails.map {
            MailDto(
                id = it.id,
                sender = it.from,
                recipients = it.to.map{ it.toString() },
                subject = it.subject,
            )
        }
        return mailDtos
    }
}