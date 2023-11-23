package systems.toni.mailcatcher.api

import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.jboss.resteasy.reactive.ResponseStatus
import systems.toni.mailcatcher.domain.MailDto
import systems.toni.mailcatcher.domain.MailSimpleDto
import systems.toni.mailcatcher.services.StorageService

@Path("/messages")
class MessagesResource {
    @Inject
    lateinit var storageService: StorageService

    companion object {
        const val MESSAGE_RFC822 = "message/rfc822"

        val mailHtmlNotFound = """
            |<html>
            |<body>
            |  <h1>No Dice</h1>
            |  <p>The message you were looking for does not exist, or doesn't have content of this type.</p>
            |</body>
            |</html>
            |
        """.trimMargin()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun messages(): List<MailSimpleDto> {
        val mails = storageService.getMails()
        return mails.map { MailSimpleDto.fromMail(it) }
    }

    @Path("/{id}.json")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun json(@PathParam("id") id: String): MailDto {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return MailDto.fromMail(mail)
    }

    @Path("/{id}.plain")
    @GET
    fun plain(@PathParam("id") id: String): Uni<Response> {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        if (mail.textBody.equals("")) {
            val response = Response
                .status(404)
                .entity(mailHtmlNotFound)
                .type(MediaType.TEXT_HTML)
                .build()
            return Uni.createFrom().item(response)
        }

        val text = mail.textBody.replace("\r\n", "\n")
        val response = Response
            .status(200)
            .entity(text)
            .type(MediaType.TEXT_PLAIN)
            .build()
        return Uni.createFrom().item(response)
    }

    @Path("/{id}.html")
    @GET
    @Produces(MediaType.TEXT_HTML)
    fun html(@PathParam("id") id: String): Uni<Response> {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        if (mail.htmlBody.equals("")) {
            return Uni.createFrom().item(Response.status(404).entity(mailHtmlNotFound).build())
        }
        val html = mail.htmlBody.replace("\r\n", "\n")
        return Uni.createFrom().item(Response.status(200).entity(html).build())
    }

    @Path("/{id}.source")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun source(@PathParam("id") id: String): String {
        val mail = storageService.getMailById(id) ?: throw NotFoundException("Mail with id $id not found")
        return mail.sourceContent
    }

    @Path("/{id}.eml")
    @GET
    @Produces(MESSAGE_RFC822)
    fun eml(@PathParam("id") id: String): String {
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