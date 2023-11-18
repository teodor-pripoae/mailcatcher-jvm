/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package systems.toni.mailcatcher.util

import java.io.*
import java.util.*
import javax.activation.DataSource
import javax.mail.*
import javax.mail.internet.*
import javax.mail.util.ByteArrayDataSource
import javax.mail.util.SharedByteArrayInputStream


/**
 * Parses a MimeMessage and stores the individual parts such a plain text,
 * HTML text and attachments.
 *
 * @since 1.3
 */
class MimeMessageParser(
    /** The MimeMessage to convert  */
    val mimeMessage: MimeMessage
) {
    /** @return Returns the mimeMessage.
     */
    /** @return Returns the plainContent if any
     */
    /** Plain mail content from MimeMessage  */
    var plainContent: String? = null
        private set
    /** @return Returns the htmlContent if any
     */
    /** Html mail content from MimeMessage  */
    var htmlContent: String? = null
        private set

    /** List of attachments of MimeMessage  */
    private val attachmentList: MutableList<DataSource>

    /** Attachments stored by their content-id  */
    private val cidMap: MutableMap<String, DataSource>
    /** @return Returns the isMultiPart.
     */
    /** Is this a Multipart email  */
    var isMultipart = false
        private set

    /**
     * Constructs an instance with the MimeMessage to be extracted.
     *
     * @param message the message to parse
     */
    init {
        attachmentList = ArrayList()
        cidMap = HashMap()
    }

    /**
     * Does the actual extraction.
     *
     * @return this instance
     * @throws Exception parsing the mime message failed
     */
    @Throws(Exception::class)
    fun parse(): MimeMessageParser {
        this.parse(null, mimeMessage)
        return this
    }

    @get:Throws(Exception::class)
    val to: List<Address>
        /**
         * @return the 'to' recipients of the message
         * @throws Exception determining the recipients failed
         */
        get() {
            val recipients = mimeMessage.getRecipients(Message.RecipientType.TO)
            return if (recipients != null) Arrays.asList(*recipients) else ArrayList()
        }

    @get:Throws(Exception::class)
    val cc: List<Address>
        /**
         * @return the 'cc' recipients of the message
         * @throws Exception determining the recipients failed
         */
        get() {
            val recipients = mimeMessage.getRecipients(Message.RecipientType.CC)
            return if (recipients != null) Arrays.asList(*recipients) else ArrayList()
        }

    @get:Throws(Exception::class)
    val bcc: List<Address>
        /**
         * @return the 'bcc' recipients of the message
         * @throws Exception determining the recipients failed
         */
        get() {
            val recipients = mimeMessage.getRecipients(Message.RecipientType.BCC)
            return if (recipients != null) Arrays.asList(*recipients) else ArrayList()
        }

    @get:Throws(Exception::class)
    val from: String?
        /**
         * @return the 'from' field of the message
         * @throws Exception parsing the mime message failed
         */
        get() {
            val addresses = mimeMessage.getFrom()
            return if (addresses == null || addresses.size == 0) {
                null
            } else (addresses[0] as InternetAddress).address
        }

    @get:Throws(Exception::class)
    val replyTo: String?
        /**
         * @return the 'replyTo' address of the email
         * @throws Exception parsing the mime message failed
         */
        get() {
            val addresses = mimeMessage.getReplyTo()
            return if (addresses == null || addresses.size == 0) {
                null
            } else (addresses[0] as InternetAddress).address
        }

    @get:Throws(Exception::class)
    val subject: String
        /**
         * @return the mail subject
         * @throws Exception parsing the mime message failed
         */
        get() = mimeMessage.getSubject()

    /**
     * Extracts the content of a MimeMessage recursively.
     *
     * @param parent the parent multi-part
     * @param part   the current MimePart
     * @throws MessagingException parsing the MimeMessage failed
     * @throws IOException        parsing the MimeMessage failed
     */
    @Throws(MessagingException::class, IOException::class)
    fun parse(parent: Multipart?, part: MimePart) {
        if (isMimeType(part, "text/plain") && plainContent == null && !Part.ATTACHMENT.equals(
                part.disposition,
                ignoreCase = true
            )
        ) {
            if (part.content is String) {
                plainContent = part.content as String
            } else if (part.content is SharedByteArrayInputStream) {
                var stream = part.content as SharedByteArrayInputStream
                plainContent = convertStreamToString(stream)
            } else {
                throw IOException("Unsupported content type: " + part.content.javaClass)
            }
        } else {
            if (isMimeType(part, "text/html") && htmlContent == null && !Part.ATTACHMENT.equals(
                    part.disposition,
                    ignoreCase = true
                )
            ) {
                htmlContent = part.content as String
            } else {
                if (isMimeType(part, "multipart/*")) {
                    isMultipart = true
                    val mp = part.content as Multipart
                    val count = mp.getCount()

                    // iterate over all MimeBodyPart
                    for (i in 0 until count) {
                        parse(mp, mp.getBodyPart(i) as MimeBodyPart)
                    }
                } else {
                    val cid = stripContentId(part.contentID)
                    val ds = createDataSource(parent, part)
                    if (cid != null) {
                        cidMap[cid] = ds
                    }
                    attachmentList.add(ds)
                }
            }
        }
    }

    /**
     * Strips the content id of any whitespace and angle brackets.
     * @param contentId the string to strip
     * @return a stripped version of the content id
     */
    private fun stripContentId(contentId: String?): String? {
        return contentId?.trim { it <= ' ' }?.replace("[\\<\\>]".toRegex(), "")
    }

    /**
     * Checks whether the MimePart contains an object of the given mime type.
     *
     * @param part     the current MimePart
     * @param mimeType the mime type to check
     * @return `true` if the MimePart matches the given mime type, `false` otherwise
     * @throws MessagingException parsing the MimeMessage failed
     * @throws IOException        parsing the MimeMessage failed
     */
    @Throws(MessagingException::class, IOException::class)
    private fun isMimeType(part: MimePart, mimeType: String): Boolean {
        // Do not use part.isMimeType(String) as it is broken for MimeBodyPart
        // and does not really check the actual content type.
        return try {
            val ct = ContentType(part.dataHandler.contentType)
            ct.match(mimeType)
        } catch (ex: ParseException) {
            part.contentType.equals(mimeType, ignoreCase = true)
        }
    }

    /**
     * Parses the MimePart to create a DataSource.
     *
     * @param parent the parent multi-part
     * @param part   the current part to be processed
     * @return the DataSource
     * @throws MessagingException creating the DataSource failed
     * @throws IOException        creating the DataSource failed
     */
    @Throws(MessagingException::class, IOException::class)
    protected fun createDataSource(parent: Multipart?, part: MimePart): DataSource {
        val dataHandler = part.dataHandler
        val dataSource = dataHandler.getDataSource()
        val contentType = getBaseMimeType(dataSource.contentType)
        val content = getContent(dataSource.inputStream)
        val result = ByteArrayDataSource(content, contentType)
        val dataSourceName = getDataSourceName(part, dataSource)
        result.name = dataSourceName
        return result
    }

    /** @return Returns the attachmentList.
     */
    fun getAttachmentList(): List<DataSource> {
        return attachmentList
    }

    val contentIds: Collection<String>
        /**
         * Returns a collection of all content-ids in the parsed message.
         *
         *
         * The content-ids are stripped of any angle brackets, i.e. "part1" instead
         * of "&lt;part1&gt;".
         *
         * @return the collection of content ids.
         * @since 1.3.4
         */
        get() = Collections.unmodifiableSet(cidMap.keys)

    /** @return true if a plain content is available
     */
    fun hasPlainContent(): Boolean {
        return plainContent != null
    }

    /** @return true if HTML content is available
     */
    fun hasHtmlContent(): Boolean {
        return htmlContent != null
    }

    /** @return true if attachments are available
     */
    fun hasAttachments(): Boolean {
        return attachmentList.size > 0
    }

    /**
     * Find an attachment using its name.
     *
     * @param name the name of the attachment
     * @return the corresponding datasource or null if nothing was found
     */
    fun findAttachmentByName(name: String): DataSource? {
        var dataSource: DataSource
        for (i in getAttachmentList().indices) {
            dataSource = getAttachmentList()[i]
            if (name.equals(dataSource.name, ignoreCase = true)) {
                return dataSource
            }
        }
        return null
    }

    /**
     * Find an attachment using its content-id.
     *
     *
     * The content-id must be stripped of any angle brackets,
     * i.e. "part1" instead of "&lt;part1&gt;".
     *
     * @param cid the content-id of the attachment
     * @return the corresponding datasource or null if nothing was found
     * @since 1.3.4
     */
    fun findAttachmentByCid(cid: String): DataSource? {
        return cidMap[cid]
    }

    /**
     * Determines the name of the data source if it is not already set.
     *
     * @param part the mail part
     * @param dataSource the data source
     * @return the name of the data source or `null` if no name can be determined
     * @throws MessagingException accessing the part failed
     * @throws UnsupportedEncodingException decoding the text failed
     */
    @Throws(MessagingException::class, UnsupportedEncodingException::class)
    protected fun getDataSourceName(part: Part, dataSource: DataSource): String? {
        var result = dataSource.name
        if (result == null || result.length == 0) {
            result = part.fileName
        }
        result = if (result != null && result.length > 0) {
            MimeUtility.decodeText(result)
        } else {
            null
        }
        return result
    }

    /**
     * Read the content of the input stream.
     *
     * @param is the input stream to process
     * @return the content of the input stream
     * @throws IOException reading the input stream failed
     */
    @Throws(IOException::class)
    private fun getContent(`is`: InputStream): ByteArray {
        var ch: Int
        val result: ByteArray
        val os = ByteArrayOutputStream()
        val isReader = BufferedInputStream(`is`)
        val osWriter = BufferedOutputStream(os)
        while (isReader.read().also { ch = it } != -1) {
            osWriter.write(ch)
        }
        osWriter.flush()
        result = os.toByteArray()
        osWriter.close()
        return result
    }

    /**
     * Parses the mimeType.
     *
     * @param fullMimeType the mime type from the mail api
     * @return the real mime type
     */
    private fun getBaseMimeType(fullMimeType: String): String {
        val pos = fullMimeType.indexOf(';')
        return if (pos >= 0) {
            fullMimeType.substring(0, pos)
        } else fullMimeType
    }

    fun convertStreamToString(inputStream: SharedByteArrayInputStream): String? {
        // Create a ByteArrayOutputStream to read the bytes from the input stream
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        try {
            while (inputStream.read(buffer).also { length = it } != -1) {
                result.write(buffer, 0, length)
            }

            // Convert the bytes to a string using appropriate encoding (e.g., UTF-8)
            return result.toString("UTF-8") // Change encoding as needed
        } catch (e: IOException) {
            throw e
        } finally {
            try {
                // Close the input stream
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
