package com.alexandr44.headhuntermonitorbot.telegram

import com.alexandr44.headhuntermonitorbot.telegram.properties.TelegramBotProperties
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update


class HeadHunterBot(
    private val handler: HeadHunterCommandHandler,
    properties: TelegramBotProperties,
) : TelegramLongPollingBot(properties.token) {

    @Value("\${telegrambot.bot.username}")
    private lateinit var botUsername: String

    override fun getBotUsername() = botUsername

    override fun onUpdateReceived(update: Update?) {
        if (update == null || !update.hasMessage()) return

        val msg: Message = update.message

        if (msg.hasText()) {
            handler.handleTextMessage(msg) { sendMessage: BotApiMethodMessage ->
                execute(sendMessage)
            }
        }
    }
}