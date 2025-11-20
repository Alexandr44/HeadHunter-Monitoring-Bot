package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.client.HeadHunterClient
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class HeadHunterService(
    private val headHunterClient: HeadHunterClient,
    private val tokenService: TokenService
) {

    companion object {
        private const val BEARER_PATTERN = "Bearer %s"
    }

    private val log = KotlinLogging.logger {}

    fun checkResumeExist(resumeId: String, tgUserId: Long): Boolean {
        val token = tokenService.getTokenByTgId(tgUserId)!!
        val response = headHunterClient.getResumeById(
            resumeId,
            String.format(BEARER_PATTERN, token.accessToken)
        )
        return response.statusCode.is2xxSuccessful
    }

}