package com.alexandr44.headhuntermonitorbot.telegram

import com.alexandr44.headhuntermonitorbot.dto.Constants
import com.alexandr44.headhuntermonitorbot.dto.MessagePattern.COMPANY_NAME_PLACEHOLDER
import com.alexandr44.headhuntermonitorbot.enums.Callback
import com.alexandr44.headhuntermonitorbot.enums.UserState
import com.alexandr44.headhuntermonitorbot.service.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.Serializable


@Service
class HeadHunterCommandHandler(
    private val menuBuilder: HeadHunterBotMenuBuilder,
    private val userService: UserService,
    private val secretService: SecretService,
    private val tokenService: TokenService,
    private val authService: AuthService,
    private val headHunterService: HeadHunterService
) {

    companion object {
        private const val SKIP = "SKIP";
    }

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
                        userService.setUserState(userId, UserState.OK)
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
                        userService.setUserState(userId, UserState.OK)
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
                        userService.setUserState(userId, UserState.OK)
                        execute(SendMessage(chatId.toString(), "✅ Сообщение отправлено в поддержку. Спасибо!"))
                    }

                    UserState.CV_ID -> {
                        headHunterService.checkResumeExist(text, chatId)
                        userService.setCvId(userId, text)
                        userService.setUserState(userId, UserState.OK)
                        execute(
                            SendMessage().apply {
                                this.chatId = chatId.toString()
                                this.text = "ID резюме: $text"
                                this.replyMarkup = menuBuilder.mainMenu()
                            }
                        )
                    }

                    UserState.CREDS_CLIENT_ID -> {
                        if (!checkInputExist(text)) {
                            resetUserState(userId)
                            execute(buildResetMessage())
                            return
                        }

                        execute(
                            clientIdProcess(userId, text)
                        )
                    }

                    UserState.CREDS_CLIENT_SECRET -> {
                        if (!checkInputExist(text)) {
                            resetUserState(userId)
                            execute(buildResetMessage())
                            return
                        }

                        execute(
                            clientSecretProcess(userId, text)
                        )
                    }

                    UserState.CREDS_CODE -> {
                        if (!checkInputExist(text)) {
                            resetUserState(userId)
                            execute(buildResetMessage())
                            return
                        }

                        secretService.saveCode(text, userId)
                        userService.setUserState(userId, UserState.CREDS_CLIENT_SECRET)
                        val secret = secretService.getUserSecretByTgId(userId)
                        execute(
                            SendMessage().apply {
                                this.chatId = chatId.toString()
                                this.text = "Супер! Теперь введите CLIENT SECRET и сможем проверить доступ, текущий " +
                                        "${secret?.clientSecret}"
                                if (secret?.clientSecret != null) {
                                    this.replyMarkup =
                                        InlineKeyboardMarkup(listOf(listOf(
                                            InlineKeyboardButton("Оставить").apply {
                                                callbackData = "${Callback.CREDS_FLOW}:${chatId}"
                                            }
                                        )))
                                }
                            }
                        )
                    }

                    UserState.CREDS_MESSAGE_PATTERN -> {
                        if (!checkInputExist(text)) {
                            resetUserState(userId)
                            execute(buildResetMessage())
                            return
                        }

                        execute(
                            messagePatternProcess(userId, text)
                        )
                    }
                }
            }
        }
    }

    fun handleCallback(
        tgChatId: Long,
        messageId: Int,
        type: String,
        data: String,
        msgSender: (BotApiMethodMessage) -> Serializable,
        msgEditor: (EditMessageReplyMarkup) -> Serializable
    ) {
        val callbackType = Callback.valueOf(type)
        when (callbackType) {
            Callback.VACANCY -> {
                val result = headHunterService.sendRequestToVacancy(tgChatId, data.toLong())
                msgEditor(
                    editButtonAfterClick(tgChatId, messageId, result)
                )
            }

            Callback.CREDS_FLOW -> {
                val user = userService.getUser(data.toLong())!!
                when (user.userState) {
                    UserState.CREDS_CLIENT_ID -> {
                        msgSender(
                            clientIdProcess(tgChatId, SKIP)
                        )

                    }

                    UserState.CREDS_CLIENT_SECRET -> {
                        msgSender(
                            clientSecretProcess(tgChatId, SKIP)
                        )
                    }

                    UserState.SUPPORT_MESSAGE -> {
                        msgSender(
                            messagePatternProcess(tgChatId, SKIP)
                        )
                    }

                    else -> {}
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

            Constants.MENU_CONFIGS -> {
                val msg = SendMessage(chatId.toString(), Constants.MENU_CONFIGS)
                msg.replyMarkup = menuBuilder.searchSettingsMenu()
                execute(msg)
            }

            Constants.MENU_CREDS -> {
                val msg = SendMessage(chatId.toString(), Constants.MENU_CREDS)
                msg.replyMarkup = menuBuilder.vacancyAlgaMenu()
                execute(msg)
            }

            Constants.CONFIG_MENU_ENTER_SEARCH_TEXT -> {
                userService.setUserState(userId, UserState.SEARCH_TEXT)
                val msg = SendMessage(
                    chatId.toString(),
                    "Введите текст для поиска. Текущий: ${userService.getUser(userId)!!.searchText}"
                )
                execute(msg)
            }

            Constants.CONFIG_MENU_ENTER_EXCLUDE_TEXT -> {
                userService.setUserState(userId, UserState.EXCLUDE_TEXT)
                val msg = SendMessage(
                    chatId.toString(),
                    "Введите исключающие слова, через запятую. Текущие: ${userService.getUser(userId)!!.excludeText}"
                )
                execute(msg)
            }

            Constants.CONFIG_MENU_SWITCH_MONITORING -> {
                val enabled = userService.switchMonitoringUser(userId)
                val msg = SendMessage(chatId.toString(), "Мониторинг " + if (enabled) "включен" else "отключен")
                execute(msg)
            }

            Constants.CREDS_MENU_ADD_CV_ID -> {
                val token = tokenService.getTokenByTgId(userId)
                if (token?.accessToken == null) {
                    val msg = SendMessage(
                        chatId.toString(),
                        "Сначала введите креды, проверка резюме невозможна"
                    )
                    execute(msg)
                    return true
                }

                userService.setUserState(userId, UserState.CV_ID)
                val msg = SendMessage(
                    chatId.toString(),
                    "Введите ID своего резюме. Текущий: ${userService.getUser(userId)!!.cvId}"
                )
                execute(msg)
            }

            Constants.CREDS_MENU_ADD_CREDS -> {
                userService.setUserState(userId, UserState.CREDS_CLIENT_ID)
                val secret = secretService.getUserSecretByTgId(userId)
                val msg = SendMessage(
                    chatId.toString(),
                    "Введите CLIENT ID. Текущий: ${secret?.clentId}"
                ).apply {
                    if (secret?.clentId != null) {
                        this.replyMarkup =
                            InlineKeyboardMarkup(listOf(listOf(
                                InlineKeyboardButton("Оставить").apply {
                                    callbackData = "${Callback.CREDS_FLOW}:${chatId}"
                                }
                            )))
                    }
                }
                execute(msg)
            }

            Constants.CREDS_MENU_ADD_TEMPLATE -> {
                userService.setUserState(userId, UserState.CREDS_MESSAGE_PATTERN)
                val messagePattern = userService.getUser(chatId)?.messagePattern
                val msg = SendMessage(
                    chatId.toString(),
                    "Введите шаблон сопроводительного письма. Допустимы плейсхолдеры: " +
                            COMPANY_NAME_PLACEHOLDER +
                            "\r\n\r\nТекущий: \r\n$messagePattern"
                ).apply {
                    if (messagePattern != null) {
                        this.replyMarkup =
                            InlineKeyboardMarkup(listOf(listOf(
                                InlineKeyboardButton("Оставить").apply {
                                    callbackData = "${Callback.CREDS_FLOW}:${chatId}"
                                }
                            )))
                    }
                }
                execute(msg)
            }

            Constants.MENU_SUPPORT -> {
                val str = """
                        |🧑‍💻 Напишите сообщение в поддержку.
                        |Мы ответим вам как можно скорее.
                        """.trimMargin()
                val msg = SendMessage(chatId.toString(), str)
                userService.setUserState(userId, UserState.SUPPORT_MESSAGE)
                execute(msg)
            }

            Constants.MENU_HELP -> {
                val str = """
                        |🧑‍💻 Напишите текст для поиска, исключающие слова через запятую и включите мониторинг.
                        """.trimMargin()
                val msg = SendMessage(chatId.toString(), str)
                execute(msg)
            }

            Constants.MENU_BACK -> {
                val msg = SendMessage(chatId.toString(), "Главное меню")
                msg.replyMarkup = menuBuilder.mainMenu()
                execute(msg)
            }

            else -> {
                return false
            }
        }
        return true
    }

    private fun checkInputExist(text: String): Boolean {
        return !(text.isEmpty() || text.isBlank() || text == "-")
    }

    private fun resetUserState(userId: Long) {
        userService.setUserState(userId, UserState.OK)
    }

    private fun buildResetMessage(): SendMessage {
        return SendMessage().apply {
            this.chatId = chatId
            this.text = "Введено пустое значение, процесс прерван"
            this.replyMarkup = menuBuilder.mainMenu()
        }
    }

    private fun clientIdProcess(tgChatId: Long, clientId: String): SendMessage {
        if (clientId != SKIP) {
            secretService.saveClientId(clientId, tgChatId)
        }
        userService.setUserState(tgChatId, UserState.CREDS_CODE)
        val url = String.format(SecretService.AUTH_URL_PATTERN, secretService.getUserSecretByTgId(tgChatId)!!.clentId)
        return SendMessage().apply {
            this.chatId = tgChatId.toString()
            this.text = "Отлично\\! Теперь пройдите по ссылке, " +
                    "авторизуйтесь и пришлите полученный code: [ТЫК]($url)"
            this.enableMarkdownV2(true)
        }
    }

    private fun clientSecretProcess(tgChatId: Long, clientSecret: String): SendMessage {
        if (clientSecret != SKIP) {
            secretService.saveClientSecret(clientSecret, tgChatId)
        }
        userService.setUserState(tgChatId, UserState.OK)
        authService.requestAccessToken(tgChatId)
        return SendMessage().apply {
            this.chatId = tgChatId.toString()
            this.text = "Поздравляю! Доступ получен!"
            this.replyMarkup = menuBuilder.mainMenu()
        }
    }

    private fun messagePatternProcess(tgChatId: Long, messagePattern: String): SendMessage {
        if (messagePattern != SKIP) {
            userService.setMessagePattern(tgChatId, messagePattern)
        }
        userService.setUserState(tgChatId, UserState.OK)
        return SendMessage().apply {
            this.chatId = tgChatId.toString()
            this.text = "Установлен шаблон сообщения: " +
                    "${userService.getUser(tgChatId)?.messagePattern}"
            this.replyMarkup = menuBuilder.mainMenu()
        }
    }

    private fun editButtonAfterClick(chatId: Long, messageId: Int, isSuccess: Boolean): EditMessageReplyMarkup {
        val markup = InlineKeyboardMarkup(listOf(
            listOf(
                InlineKeyboardButton(
                    if (isSuccess) "✅ Отправлено" else "⚠ Не удалось отправить"
                ).apply {
                    callbackData = "done"
                }
            )
        ))

        return EditMessageReplyMarkup().apply {
            this.chatId = chatId.toString()
            this.messageId = messageId
            this.replyMarkup = markup
        }
    }

}