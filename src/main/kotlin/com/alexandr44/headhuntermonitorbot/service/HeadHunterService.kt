package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.client.HeadHunterClient
import com.alexandr44.headhuntermonitorbot.dto.MessagePattern
import com.alexandr44.headhuntermonitorbot.exception.BotLogicException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap

@Service
class HeadHunterService(
    private val headHunterClient: HeadHunterClient,
    private val tokenService: TokenService,
    private val userService: UserService
) {

    companion object {
        private const val BEARER_PATTERN = "Bearer %s"
    }

    private val log = KotlinLogging.logger {}

    fun checkResumeExist(resumeId: String, tgUserId: Long): Boolean {
        val token = tokenService.getTokenByTgId(tgUserId)!!
        val response = headHunterClient.getResumeById(
            String.format(BEARER_PATTERN, token.accessToken),
            resumeId
        )
        return response.statusCode.is2xxSuccessful
    }

    fun sendRequestToVacancy(tgChatId: Long, vacancyId: Long): Boolean {
        val user = userService.getUser(tgChatId)!!

        val response = headHunterClient.getVacancy(vacancyId)
        if (!response.statusCode.is2xxSuccessful) {
            log.error { "Vacancy $vacancyId not found, response code ${response.statusCode}, body ${response.body}" }
            throw BotLogicException("Vacancy $vacancyId not found, status code: ${response.statusCode}")
        }
        val vacancy = response.body!!

        val messagePattern = user.messagePattern ?: throw BotLogicException("Message pattern missing")
        val message = messagePattern.replace(MessagePattern.COMPANY_NAME_PLACEHOLDER, vacancy.employer.name)

        val token = tokenService.getTokenByTgId(tgChatId)!!
        val applyResponse = headHunterClient.applyToVacancy(
            String.format(BEARER_PATTERN, token.accessToken),
            LinkedMultiValueMap<String, String>().apply {
                add("resume_id", user.cvId ?: throw BotLogicException("Cv ID missing"))
                add("vacancy_id", vacancyId.toString())
                add("message", message)
            }
        )

        return applyResponse.statusCode.is2xxSuccessful
    }

}