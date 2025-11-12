package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.client.HeadHunterClient
import com.alexandr44.headhuntermonitorbot.dto.VacancyDto
import com.alexandr44.headhuntermonitorbot.repository.UserRepository
import com.alexandr44.headhuntermonitorbot.repository.VacancyIdRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class HeadHunterService(
    val headHunterClient: HeadHunterClient,
    val userRepository: UserRepository,
    val vacancyIdRepository: VacancyIdRepository
) {

    companion object {
        private const val SCHEDULE = "remote"
        private const val ORDER = "publication_time"
        private const val PAGE_SIZE = 50
    }

    private val log = KotlinLogging.logger {}

    fun checkVacancies() {
        val users = userRepository.findAllByActiveIsTrue()

        for (user in users) {
            log.info("Processing users: ${user.username}")
            val checkedVacanciesIds = vacancyIdRepository.findAllByUserId(user.id).map { it.id }

            val excludeWords = user.excludeText.split(",")
            val vacancies = getVacanciesForToday(user.searchText)
                .filter { vacancyDto -> !excludeWords.any { vacancyDto.name.contains(it, ignoreCase = true) } }
                .filter { vacancyDto -> !checkedVacanciesIds.contains(vacancyDto.id) }

            log.info("Got response: ${vacancies}")
        }
    }

    private fun getVacanciesForToday(text: String): List<VacancyDto> {
        val vacancies: MutableList<VacancyDto> = mutableListOf()
        val today = OffsetDateTime.now()

        var isFinished = false
        var page = 0;

        while (!isFinished) {
            val response = headHunterClient.getVacancies(
                text,
                SCHEDULE,
                ORDER,
                PAGE_SIZE,
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