package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.client.HeadHunterClient
import com.alexandr44.headhuntermonitorbot.dto.VacancyDto
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class HeadHunterService(
    val headHunterClient: HeadHunterClient
) {

    private val log = KotlinLogging.logger {}

    fun checkVacancies() {
        val vacancies = getVacanciesForToday()
        log.info("Got response: ${vacancies}")
    }

    fun getVacanciesForToday(): List<VacancyDto> {
        val vacancies: MutableList<VacancyDto> = mutableListOf()
        val today = OffsetDateTime.now()

        var isFinished = false
        var page = 0;

        while (!isFinished) {
            val response = headHunterClient.getVacancies(
                "java",
                "remote",
                "publication_time",
                50,
                page++
            )
            val vacancyList = response.body?.items
            if (vacancyList == null) {
                isFinished = true
                continue
            }

            for (vacancy in vacancyList) {
                if (vacancy.publishedAt.dayOfMonth != today.dayOfMonth ||
                    vacancy.publishedAt.monthValue != today.monthValue ||
                    vacancy.publishedAt.year != today.year
                ) {
                    isFinished = true
                } else {
                    vacancies.add(vacancy)
                }
            }
        }

        return vacancies
    }

}