package com.alexandr44.headhuntermonitorbot.telegram

import com.alexandr44.headhuntermonitorbot.dto.Constants
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class HeadHunterCommandHandler(
    val menuBuilder: HeadHunterBotMenuBuilder,
//    val printService: PrintService,
//    val userService: UserService,
) {

    private val log = KotlinLogging.logger {}

    @Value("\${telegrambot.bot.support_chat_id}")
    private lateinit var supportChatId: String

    @Value("\${telegrambot.bot.token}")
    private lateinit var botToken: String

    fun handleTextMessage(message: Message, execute: (BotApiMethodMessage) -> Message) {
        val chatId: Long = message.chatId
        val text: String = message.text
        val userId: Long = message.from.id

        if (text == "/start") {
            val msg = SendMessage(chatId.toString(), "Добро пожаловать! Пропишите поисковое слово и включите мониторинг для получения вакансий")
            msg.replyMarkup = menuBuilder.mainMenu()
            execute(msg)
            return
        } else if (text.startsWith("/reply")) {
            handleReplyMessage(text, execute)
        } else {
//            if (!handleMenuButtons(text, chatId, userId, execute)) {
//                when(userService.getUserState(userId)) {
//                    UserState.OK -> execute(SendMessage(chatId.toString(), "Ничего не знаю, файл давай"))
//                    UserState.SUPPORT_MESSAGE -> {
//                        val supportMsg = """
//                            🆘 Новое сообщение от пользователя:
//                            👤 ID: %d
//                            🔗 @%s
//                            💬 %s
//                            """.trimIndent().format(userId, message.from.userName ?: "без username", text)
//
//                        val toSupport = SendMessage()
//                        toSupport.chatId = supportChatId
//                        toSupport.text = supportMsg
//                        execute.invoke(toSupport)
//                        userService.setUserState(userId, UserState.OK)
//                        execute(SendMessage(chatId.toString(), "✅ Сообщение отправлено в поддержку. Спасибо!"))
//                    }
//                }
//            }
        }
    }

    private fun handleReplyMessage(text: String, execute: (BotApiMethodMessage) -> Message) {
        val parts: List<String> = text.split(" ")
        if (parts.size < 3) {
            execute(SendMessage(supportChatId, "❌ Использование: /reply <chatId> <текст>"))
            return
        }

        val chatId: Long = parts[1].toLong()
        val replyText = parts.drop(2).joinToString(" ")

        val msg = SendMessage(chatId.toString(), "💬 Ответ поддержки:\n$replyText")
        try {
            execute(msg)
            execute(SendMessage(supportChatId, "✅ Ответ отправлен пользователю $chatId"))
        } catch (e: TelegramApiException) {
            execute(SendMessage(supportChatId, "❌ Не удалось отправить сообщение пользователю."))
            e.printStackTrace()
        }
    }

    private fun handleMenuButtons(
        text: String,
        chatId: Long,
        userId: Long,
        execute: (BotApiMethodMessage) -> Message
    ): Boolean {
        when (text) {

            Constants.MENU_ENTER_SEARCH_TEXT -> {
//                userService.setUserPageLayout(userId, PageLayout.ONE)
//                val msg = SendMessage(chatId.toString(), "Выбрана печать 1 страница на 1")
//                execute(msg)
            }

            Constants.MENU_ENTER_EXCLUDE_TEXT -> {
//                userService.setUserPageLayout(userId, PageLayout.TWO)
//                val msg = SendMessage(chatId.toString(), "Выбрана печать 2 страницы на 1")
//                execute(msg)
            }

            Constants.MENU_SWITCH_MONITORING -> {
//                userService.setUserPageLayout(userId, PageLayout.FOUR)
//                val msg = SendMessage(chatId.toString(), "Выбрана печать 4 страницы на 1")
//                execute(msg)
            }

            Constants.MENU_SUPPORT -> {
                val str = """
                        |🧑‍💻 Напишите сообщение в поддержку.
                        |Мы ответим вам как можно скорее.
                        """.trimMargin()
//                val msg = SendMessage(chatId.toString(), str)
//                userService.setUserState(userId, UserState.SUPPORT_MESSAGE)
//                execute(msg)
            }

            Constants.MENU_HELP -> {
                val str = """
                        |🧑‍💻 Пришли мне файлик и я его распечатаю.
                        |В меню можно выбрать, сколько страниц печатать на одной странице.
                        """.trimMargin()
                val msg = SendMessage(chatId.toString(), str)
                execute(msg)
            }

            else -> {
                return false
            }
        }
        return true
    }


}