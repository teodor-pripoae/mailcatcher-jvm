package systems.toni.mailcatcher.api

import jakarta.ws.rs.core.MediaType
import jakarta.inject.Inject
import jakarta.ws.rs.*
import org.jboss.resteasy.reactive.ResponseStatus
import systems.toni.mailcatcher.domain.MailDto
import systems.toni.mailcatcher.domain.MailDtoMapper
import systems.toni.mailcatcher.services.StorageService

@Path("/messages")
class MessagesResource {
    @Inject
    lateinit var storageService: StorageService

    companion object {
        const val MESSAGE_RFC822 = "message/rfc822"
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun messages(): List<MailDto> {
        val mails = storageService.getMails()
        val mailDtos = mails.map {
            MailDtoMapper.map(it)
        }
        return mailDtos
    }

    @Path("/{id}.json")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun json(@PathParam("id") id: String): MailDto {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return MailDtoMapper.map(mail)
    }

    @Path("/{id}.plain")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun plain(id: String): String {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return mail.textBody
    }

    @Path("/{id}.source")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun source(id: String): String {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return mail.sourceContent
    }

    @Path("/{id}.html")
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun html(id: String): String {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return mail.htmlBody
    }

    @Path("/{id}.eml")
    @GET
    @Produces(MESSAGE_RFC822)
    fun eml(id: String): String {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return mail.sourceContent
    }

    @DELETE
    @ResponseStatus(204)
    fun deleteAll() {
        storageService.deleteAll()
    }

    @Path("/{id}")
    @DELETE
    @ResponseStatus(204)
    fun delete(@PathParam("id") id: String) {
        storageService.deleteById(id) ?: throw NotFoundException("Mail with id $id not found")
    }
}