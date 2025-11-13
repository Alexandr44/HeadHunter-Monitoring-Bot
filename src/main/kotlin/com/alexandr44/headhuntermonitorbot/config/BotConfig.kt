package com.alexandr44.headhuntermonitorbot.config

import com.alexandr44.headhuntermonitorbot.telegram.HeadHunterBot
import com.alexandr44.headhuntermonitorbot.telegram.HeadHunterCommandHandler
import com.alexandr44.headhuntermonitorbot.telegram.properties.TelegramBotProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.bots.TelegramLongPollingBot

@Configuration
class BotConfig {

    @Bean
    fun telegramHeadHunterBot(
        handler: HeadHunterCommandHandler,
        properties: TelegramBotProperties
    ): TelegramLongPollingBot {
        return HeadHunterBot(handler, properties)
    }

}