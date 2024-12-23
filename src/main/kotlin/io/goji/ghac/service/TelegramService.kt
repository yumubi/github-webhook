package io.goji.io.goji.ghac.service

import io.goji.ghac.config.Config
import io.goji.ghac.plugin.TemplateEngine
import io.goji.io.goji.ghac.model.Location
import io.vertx.core.json.JsonObject
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.slf4j.LoggerFactory
import java.io.File




class TelegramService(
    private val config: Config,
    private val templateEngine: TemplateEngine
    ) {
    private val logger = LoggerFactory.getLogger(TelegramService::class.java)
    private val bot: TelegramLongPollingBot


    init {
        val botOptions = DefaultBotOptions().apply {
            config.settings.socks5Proxy?.let {
                proxyHost = it.substringBefore(":")
                proxyPort = it.substringAfter(":").toInt()
                proxyType = DefaultBotOptions.ProxyType.SOCKS5
            }
        }

        bot = object : TelegramLongPollingBot(botOptions, config.token) {
            override fun getBotUsername() = "GitHubWebhookBot"
            override fun onUpdateReceived(update: org.telegram.telegrambots.meta.api.objects.Update) {
                // Webhook bot doesn't need to handle updates
            }
        }
    }

    suspend fun sendMessages(payload: JsonObject) {
        val authorEmail = payload.getString("author_email")
        val chatIds = parseChatIds(config.recipients, authorEmail)

        chatIds.forEach { chatId ->
            try {

                // Send text messages
                sendTextMessage(chatId, payload)
                // Send media if configured
                sendConfiguredMedia(chatId)

            } catch (e: Exception) {
                logger.error("Failed to send message to chat $chatId", e)
            }
        }
    }




    private suspend fun sendTextMessage(chatId: Long, payload: JsonObject) {
        val message = when {
            config.message.text != null -> config.message.text
            config.message.messageFile != null -> File(config.message.messageFile).readText()
            //else -> createDefaultMessage(context)
            else -> "Hello World"
        }

        val renderedMessage = templateEngine.render(message.toString(), mapOf("payload" to payload))




        val sendMessage = SendMessage().apply {
            this.chatId = chatId.toString()
            //text = renderedMessage

            // or just use the message
            text = message

            parseMode = config.message.format
            disableWebPagePreview = config.settings.disableWebPagePreview
            disableNotification = config.settings.disableNotification
        }

        bot.execute(sendMessage)
    }


    private fun sendConfiguredMedia(chatId: Long) {
        // Send photos

        config.media.photos.forEach { sendPhoto(chatId, it) }

        // Send documents
        config.media.documents.forEach { document ->
            sendDocument(chatId, document)
        }

        // Send stickers
        config.media.stickers.forEach { sticker ->
            sendSticker(chatId, sticker)
        }

        // Send locations
        config.media.location.forEach { location ->
            sendLocation(chatId, location)
        }
    }

    private fun sendPhoto(chatId: Long, photoPath: String) {
        val photo = SendPhoto().apply {
            this.chatId = chatId.toString()
            this.photo = InputFile(File(photoPath))
        }
        try {
            bot.execute(photo)
        } catch (e: Exception) {
            logger.error("Failed to send photo: $photoPath", e)
        }
    }

    private fun sendDocument(chatId: Long, documentPath: String) {
        val document = SendDocument().apply {
            this.chatId = chatId.toString()
            this.document = InputFile(File(documentPath))
        }
        try {
            bot.execute(document)
        } catch (e: Exception) {
            logger.error("Failed to send document: $documentPath", e)
        }
    }

    private fun sendSticker(chatId: Long, stickerPath: String) {
        val sticker = SendSticker().apply {
            this.chatId = chatId.toString()
            this.sticker = InputFile(File(stickerPath))
        }
        try {
            bot.execute(sticker)
        } catch (e: Exception) {
            logger.error("Failed to send sticker: $stickerPath", e)
        }
    }

    private fun sendLocation(chatId: Long, locationString: String) {
        val location = parseLocation(locationString) ?: return

        val sendLocation = SendLocation().apply {
            this.chatId = chatId.toString()
            this.latitude = location.latitude
            this.longitude = location.longitude
        }
        try {
            bot.execute(sendLocation)
        } catch (e: Exception) {
            logger.error("Failed to send location", e)
        }
    }

    private fun parseChatIds(to: List<String>, authorEmail: String?): List<Long> {
        val emails = mutableListOf<Long>()
        val ids = mutableListOf<Long>()
        var attachEmail = true

        to.forEach { value ->
            val parts = value.trim().split(":")
            val id = parts[0].toLongOrNull()
            if (id != null) {
                if (parts.size > 1) {
                    if (parts[1] == authorEmail) {
                        emails.add(id)
                        attachEmail = false
                    }
                } else {
                    ids.add(id)
                }
            }
        }

        return if (config.settings.matchEmail && !attachEmail) emails else ids + emails
    }

    private fun parseLocation(locationString: String): Location? {
        val parts = locationString.trim().split(" ")
        if (parts.size < 2) return null

        return try {
            Location(
                latitude = parts[0].toDouble(),
                longitude = parts[1].toDouble(),
                title = if (parts.size > 2) parts[2] else "",
                address = if (parts.size > 3) parts[3] else ""
            )
        } catch (e: Exception) {
            logger.error("Failed to parse location: $locationString", e)
            null
        }
    }



    private fun createDefaultMessage(context: JsonObject): String {

//        return if (context.config.ci.build) {
//            "${context.repo.fullName}/${context.github.workflow} triggered by ${context.repo.namespace} (${context.github.eventName})"
//        } else {
//                """
//                ${getStatusIcon(context.build.status)} Build #${context.build.number} of `${context.repo.fullName}` ${context.build.status}.
//
//                📝 Commit by ${context.commit.author} on `${context.commit.branch}`:
//                ``` ${context.commit.message} ```
//
//                🌐 ${context.build.link}
//            """.trimIndent()
//        }
        // todo
        return context.encodePrettily()
    }

    private fun getStatusIcon(status: String): String = when (status.lowercase()) {
        "failure" -> "❌"
        "cancelled" -> "❕"
        "success" -> "✅"
        else -> "ℹ️"
    }



    fun escapeMarkdown(text: String): String {
        return text.replace("_", "\\_")
    }
}
