package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.dto.VacancyDto
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Service
class TelegramService(
    val telegramBot: TelegramLongPollingBot
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

        val builder = StringBuilder()
        builder.append("Новая вакансия")
            .append(vacancyDto.name)
            .append("Компания: ${vacancyDto.employer.name}")
            .append("ЗП: $salaryMsg")
            .append(vacancyDto.url)

        val msg = SendMessage(chatId.toString(), builder.toString())
        telegramBot.execute(msg)
    }

}