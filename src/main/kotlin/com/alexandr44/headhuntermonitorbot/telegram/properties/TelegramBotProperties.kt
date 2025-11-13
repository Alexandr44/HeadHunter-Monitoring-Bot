package com.alexandr44.headhuntermonitorbot.telegram.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegrambot.bot")
class TelegramBotProperties {
    lateinit var token: String
}