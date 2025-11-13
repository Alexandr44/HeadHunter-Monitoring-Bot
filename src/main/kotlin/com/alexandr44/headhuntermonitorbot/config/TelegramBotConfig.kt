package com.alexandr44.headhuntermonitorbot.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
class TelegramBotConfig {

    @Bean
    fun botsApi(telegramBot: List<TelegramLongPollingBot>): TelegramBotsApi {
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        for (bot in telegramBot) {
            botsApi.registerBot(bot)
        }
        return botsApi
    }

}