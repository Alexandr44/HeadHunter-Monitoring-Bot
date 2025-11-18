package com.alexandr44.headhuntermonitorbot.telegram

import com.alexandr44.headhuntermonitorbot.dto.Constants
import com.alexandr44.headhuntermonitorbot.enums.UserState
import com.alexandr44.headhuntermonitorbot.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class HeadHunterCommandHandler(
    private val menuBuilder: HeadHunterBotMenuBuilder,
    private val userService: UserService,
) {

    @Value("\${telegrambot.bot.support_chat_id}")
    private lateinit var supportChatId: String

    fun handleTextMessage(message: Message, execute: (BotApiMethodMessage) -> Message) {
        val chatId: Long = message.chatId
        val text: String = message.text
        val userId: Long = message.from.id

        if (text == "/start") {
            userService.addNewUser(chatId, message.from.userName)
            val toSupport = SendMessage()
            toSupport.chatId = supportChatId
            toSupport.text = """
                            🆘 Новый пользователь:
                            👤 ID: %d
                            🔗 @%s
                            """.trimIndent().format(userId, message.from.userName ?: "без username")
            execute.invoke(toSupport)
            val msg = SendMessage(
                chatId.toString(),
                "Добро пожаловать! Пропишите поисковое слово и включите мониторинг для получения вакансий"
            )
            msg.replyMarkup = menuBuilder.mainMenu()
            execute(msg)
            return
        } else if (text.startsWith("/reply")) {
            handleReplyMessage(text, execute)
        } else {
            if (!handleMenuButtons(text, chatId, userId, execute)) {
                when (userService.getUserState(userId)) {
                    UserState.OK -> execute(
                        SendMessage().apply {
                            this.chatId = chatId.toString()
                            this.text = "Пу-пу-пу..."
                            this.replyMarkup = menuBuilder.mainMenu()
                        }
                    )

                    UserState.SEARCH_TEXT -> {
                        userService.setSearchText(userId, text)
                        userService.saveUserState(userId, UserState.OK)
                        execute(
                            SendMessage().apply {
                                this.chatId = chatId.toString()
                                this.text = "Поисковое слово: $text"
                                this.replyMarkup = menuBuilder.mainMenu()
                            }
                        )
                    }

                    UserState.EXCLUDE_TEXT -> {
                        userService.setExcludeText(userId, text)
                        userService.saveUserState(userId, UserState.OK)
                        execute(
                            SendMessage().apply {
                                this.chatId = chatId.toString()
                                this.text = "Исключающие слова: $text"
                                this.replyMarkup = menuBuilder.mainMenu()
                            }
                        )
                    }

                    UserState.SUPPORT_MESSAGE -> {
                        val supportMsg = """
                            🆘 Новое сообщение от пользователя:
                            👤 ID: %d
                            🔗 @%s
                            💬 %s
                            """.trimIndent().format(userId, message.from.userName ?: "без username", text)

                        execute.invoke(
                            SendMessage().apply {
                                this.chatId = supportChatId
                                this.text = supportMsg
                                this.replyMarkup = menuBuilder.mainMenu()
                            }
                        )
                        userService.saveUserState(userId, UserState.OK)
                        execute(SendMessage(chatId.toString(), "✅ Сообщение отправлено в поддержку. Спасибо!"))
                    }
                }
            }
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
                userService.saveUserState(userId, UserState.SEARCH_TEXT)
                val msg = SendMessage(
                    chatId.toString(),
                    "Введите текст для поиска. Текущий: ${userService.getUser(userId).searchText}"
                )
                execute(msg)
            }

            Constants.MENU_ENTER_EXCLUDE_TEXT -> {
                userService.saveUserState(userId, UserState.EXCLUDE_TEXT)
                val msg = SendMessage(
                    chatId.toString(),
                    "Введите исключающие слова, через запятую. Текущие: ${userService.getUser(userId).excludeText}"
                )
                execute(msg)
            }

            Constants.MENU_SWITCH_MONITORING -> {
                val enabled = userService.switchUser(userId)
                val msg = SendMessage(chatId.toString(), "Мониторинг " + if (enabled) "включен" else "отключен")
                execute(msg)
            }

            Constants.MENU_SUPPORT -> {
                val str = """
                        |🧑‍💻 Напишите сообщение в поддержку.
                        |Мы ответим вам как можно скорее.
                        """.trimMargin()
                val msg = SendMessage(chatId.toString(), str)
                userService.saveUserState(userId, UserState.SUPPORT_MESSAGE)
                execute(msg)
            }

            Constants.MENU_HELP -> {
                val str = """
                        |🧑‍💻 Напишите текст для поиска, исключающие слова через запятую и включите мониторинг.
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