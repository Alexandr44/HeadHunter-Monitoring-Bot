package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.dto.response.VacancyDto
import com.alexandr44.headhuntermonitorbot.entity.AutoReplyResult
import com.alexandr44.headhuntermonitorbot.enums.Callback
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.time.Instant

@Service
class TelegramService(
    private val telegramBot: TelegramLongPollingBot
) {

    fun sendVacancy(vacancyDto: VacancyDto, chatId: Long, autoReplyResult: AutoReplyResult) {
        val salaryMsg =
            if (vacancyDto.salary != null) {
                "от ${vacancyDto.salary.from} до ${vacancyDto.salary.to}"
            } else if (vacancyDto.salaryRange != null) {
                "от ${vacancyDto.salaryRange.from} до ${vacancyDto.salaryRange.to}"
            } else {
                "Не указано"
            }

        val message = """
            *Новая вакансия!*
            *${vacancyDto.name}*
            Компания: _${vacancyDto.employer.name}_
            💰 ЗП: $salaryMsg
            🔗 [Открыть вакансию](${vacancyDto.alternateUrl})
        """.trimIndent()

        val msg = SendMessage(chatId.toString(), message)
        msg.parseMode = "Markdown"
        msg.replyMarkup = buildReplyButton(vacancyDto, autoReplyResult)
        telegramBot.execute(msg)
    }

    private fun buildReplyButton(vacancyDto: VacancyDto, autoReplyResult: AutoReplyResult): InlineKeyboardMarkup {
        val row = mutableListOf<InlineKeyboardButton>()

        if (vacancyDto.hasTest) {
            row += InlineKeyboardButton("🚫 Недоступно - есть тест").apply {
                callbackData = "none"
            }
        } else {
            when(autoReplyResult) {
                AutoReplyResult.SUCCESS -> {
                    row += InlineKeyboardButton("✅ Авто-отклик отправлен (${Instant.now()})").apply {
                        callbackData = "none"
                    }
                }
                AutoReplyResult.FAILED -> {
                    row += InlineKeyboardButton("⚠ Откликнуться (авто-отклик не прошёл)").apply {
                        callbackData = "${Callback.VACANCY}:${vacancyDto.id}"
                    }
                }
                AutoReplyResult.NONE -> {
                    row += InlineKeyboardButton("Откликнуться").apply {
                        callbackData = "${Callback.VACANCY}:${vacancyDto.id}"
                    }
                }
            }
        }

        return InlineKeyboardMarkup(listOf(row))
    }

}