package com.alexandr44.headhuntermonitorbot.telegram

import com.alexandr44.headhuntermonitorbot.exception.BotLogicException
import com.alexandr44.headhuntermonitorbot.telegram.properties.TelegramBotProperties
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update


class HeadHunterBot(
    private val handler: HeadHunterCommandHandler,
    properties: TelegramBotProperties,
) : TelegramLongPollingBot(properties.token) {

    @Value("\${telegrambot.bot.username}")
    private lateinit var botUsername: String

    private val log = KotlinLogging.logger {}

    override fun getBotUsername() = botUsername

    override fun onUpdateReceived(update: Update?) {
        if (update == null) return

        try {
            if (update.hasCallbackQuery()) {
                val callbackData = update.callbackQuery.data
                val items = callbackData.split(":")
                if (items.size < 2) return
                val type = items[0]
                val data = items[1]
                handler.handleCallback(update.callbackQuery.from.id, type, data) { sendMessage: BotApiMethodMessage ->
                    execute(sendMessage)
                }
            } else if (update.hasMessage()) {
                val msg: Message = update.message

                if (msg.hasText()) {
                    handler.handleTextMessage(msg) { sendMessage: BotApiMethodMessage ->
                        execute(sendMessage)
                    }
                }
            }
        } catch (e: BotLogicException) {
            log.error { "Got logic exception " + e.message + " with request from chat id " + update.message.from.id }
            e.printStackTrace()
            execute(
                SendMessage(
                    update.message.chatId.toString(),
                    """
                    🆘 Получена ошибка в логике: %s
                    """.trimIndent().format(e.message)
                )
            )
        } catch (e: Exception) {
            log.error { "Got exception " + e.message + " with request from chat id " + update.message.from.id }
            e.printStackTrace()
            execute(
                SendMessage(
                    update.message.chatId.toString(),
                    """
                    🆘 Получена ошибка при выполнении: %s
                    """.trimIndent().format(e.message)
                )
            )
        }
    }
}