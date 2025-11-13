package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.dto.VacancyDto
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Service
class TelegramService(
    private val telegramBot: TelegramLongPollingBot
) {

    fun sendVacancies(vacancyDtoList: List<VacancyDto>, chatId: Long) {
        for (vacancy in vacancyDtoList) {
            sendVacancy(vacancy, chatId)
        }
    }

    private fun sendVacancy(vacancyDto: VacancyDto, chatId: Long) {
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
        telegramBot.execute(msg)
    }

}