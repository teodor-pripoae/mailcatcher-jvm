package systems.toni.mailcatcher.api

import jakarta.ws.rs.core.MediaType
import jakarta.inject.Inject
import jakarta.ws.rs.*
import systems.toni.mailcatcher.domain.MailDto
import systems.toni.mailcatcher.domain.MailDtoMapper
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
            MailDtoMapper.map(it)
        }
        return mailDtos
    }

    @Path("/{id}.plain")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun plain(id: String): String {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return mail.textBody
    }

    @Path("/{id}.html")
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun html(id: String): String {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return mail.htmlBody
    }

    @DELETE
    fun deleteAll() {
        storageService.deleteAll()
    }
}