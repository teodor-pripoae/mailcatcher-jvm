package systems.toni.mailcatcher.domain

import jakarta.mail.internet.InternetAddress
import org.simplejavamail.converter.internal.mimemessage.MimeDataSource
import java.time.Instant

data class Attachment(
    val filename: String,
    val type: String,
    val size: Int,
) {
    var content: ByteArray = byteArrayOf()
        private set

    companion object {
        fun fromMimeDataSource(ds: MimeDataSource): Attachment {
            var content = ds.dataSource.inputStream.readAllBytes()

            // if the content was transfered using 7bit encoding
            // we need to remove the \r\n from the end of each line

            if (ds.contentTransferEncoding.equals("7bit")) {
                content = content.map {
                    it.toInt().toChar()
                }.joinToString("").replace("\r\n", "\n").toByteArray()
            }

            val attachment = Attachment(
                filename = ds.dataSource.name,
                type = ds.dataSource.contentType,
                size = content.size,
            )
            attachment.content = content
            return attachment
        }
    }
}

class Address(
    private val jakartaAddress: InternetAddress,
) {
    override fun toString(): String {
        // RFC specifies that the address uses "" to escape special characters
        // but since we are enclosing it in <> we need to remove the quotes
        // at least this is what mailcatcher ruby does
        return "<${jakartaAddress}>".replace("\"", "")
    }

    fun emailFormatted(): String {
        return "<${jakartaAddress.address}>"
    }
}

data class Mail(
    var id: Int = 0,
    val from: String,
    val to: List<Address>,
    val subject: String,
    val textBody: String,
    val htmlBody: String,
    val attachments: List<Attachment>,
    val receivedAt: Instant,
    val messageId: String,
    val contentType: String,
) {
    var sourceContent: String = ""

    fun createdAt(): String {
        return receivedAt.toString()
    }

    fun size(): Int {
        return sourceContent.length
    }

    fun formats(): List<String> {
        var formats: List<String> = listOf()
        if (!sourceContent.equals("")) {
            formats = formats.plus("source")
        }

        if (!htmlBody.equals("")) {
            formats = formats.plus("html")
        }
        if (!textBody.equals("")) {
            formats = formats.plus("plain")
        }
        return formats
    }
}