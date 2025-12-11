package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.client.HeadHunterClient
import com.alexandr44.headhuntermonitorbot.dto.response.VacancyDto
import com.alexandr44.headhuntermonitorbot.entity.AutoReplyResult
import com.alexandr44.headhuntermonitorbot.entity.User
import com.alexandr44.headhuntermonitorbot.entity.VacancyId
import com.alexandr44.headhuntermonitorbot.repository.TokenRepository
import com.alexandr44.headhuntermonitorbot.repository.UserRepository
import com.alexandr44.headhuntermonitorbot.repository.VacancyIdRepository
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class HeadHunterVacancyMonitorService(
    private val headHunterClient: HeadHunterClient,
    private val userRepository: UserRepository,
    private val vacancyIdRepository: VacancyIdRepository,
    private val telegramService: TelegramService,
    private val tokenRepository: TokenRepository,
    private val authService: AuthService,
    private val headHunterService: HeadHunterService,
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
            if (user.searchText.isBlank()) {
                continue
            }

            val texts = user.searchText.split(";").map { it.trim() }
            for (text in texts) {
                log.info("Processing users: ${user.username}, searching: $text")
                val checkedVacanciesIds = vacancyIdRepository.findAllByUserId(user.id!!).map { it.vacancyId }

                val excludeWords = user.excludeText.split(",").map { it.trim() }
                val vacancies = getVacanciesForToday(text)
                    .filter { vacancyDto -> !excludeWords.any { vacancyDto.name.contains(it, ignoreCase = true) } }
                    .filter { vacancyDto -> !checkedVacanciesIds.contains(vacancyDto.id) }

                log.info("Vacancies: ${vacancies.size}")

                for (vacancy in vacancies) {
                    val autoReplyResult = checkForAutoReply(user, vacancy, text)
                    telegramService.sendVacancy(vacancy, user.userChatId, autoReplyResult)
                    vacancyIdRepository.save(
                        VacancyId(
                            vacancyId = vacancy.id,
                            userId = user.id!!,
                        )
                    )
                }
            }
        }
    }

    @Transactional
    fun refreshTokens() {
        log.info("Refreshing tokens")
        val tokenList = tokenRepository.findAll()
        for (token in tokenList) {
            if (token.refreshToken.isNullOrEmpty()) {
                continue
            }
            val expiration = Instant.ofEpochSecond(token.expiredAt!!)
            if (expiration.minus(2, ChronoUnit.DAYS).isAfter(Instant.now())) {
                continue
            }

            val tokenDto = authService.refreshAccessToken(token.userId, token.refreshToken!!)
            token.accessToken = tokenDto.accessToken
            token.refreshToken = tokenDto.refreshToken
            token.expiredAt = System.currentTimeMillis() / 1000 + tokenDto.expiresIn
            log.info("Refreshed token of user: ${token.userId}")
        }
    }

    private fun getVacanciesForToday(text: String): List<VacancyDto> {
        val vacancies: MutableList<VacancyDto> = mutableListOf()

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
                if (checkVacancyDate(vacancy)) {
                    vacancies.add(vacancy)
                } else {
                    isFinished = true
                }
            }
        }

        return vacancies
    }

    private fun checkVacancyDate(vacancy: VacancyDto): Boolean {
        val vacancyDate = vacancy.publishedAt.toLocalDate()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        return vacancyDate.equals(today) || vacancyDate.equals(yesterday)
    }

    private fun checkForAutoReply(user: User, vacancy: VacancyDto, searchText: String): AutoReplyResult {
        if (user.autoReplyEnabled) {
            if (vacancy.hasTest) {
                return AutoReplyResult.NONE
            }

            if (vacancy.name.contains(searchText, ignoreCase = true)) {
                log.info(
                    "Sending auto-reply for user: ${user.userChatId} by searching: $searchText " +
                            "for vacancy: ${vacancy.id}"
                )
                try {
                    val result = headHunterService.sendRequestToVacancy(user.userChatId, vacancy.id)
                    return if (result) AutoReplyResult.SUCCESS else AutoReplyResult.FAILED
                } catch (e: Exception) {
                    e.printStackTrace()
                    log.error {
                        "Got exception, while sending auto reply for user: ${user.userChatId} " +
                                "by searching: $searchText for vacancy: ${vacancy.id}, error: ${e.message}"
                    }
                    return AutoReplyResult.FAILED
                }
            }
        }
        return AutoReplyResult.NONE
    }

}