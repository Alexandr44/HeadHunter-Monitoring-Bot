package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.client.HeadHunterClient
import com.alexandr44.headhuntermonitorbot.dto.TokenRefreshRequest
import com.alexandr44.headhuntermonitorbot.dto.TokenRequest
import com.alexandr44.headhuntermonitorbot.dto.response.TokenDto
import com.alexandr44.headhuntermonitorbot.exception.BotLogicException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val tokenService: TokenService,
    private val secretService: SecretService,
    private val headHunterClient: HeadHunterClient
) {

    @Value("\${hh.redirect_url}")
    private lateinit var redirectUrl: String

    @Value("\${hh.grant_type_access}")
    private lateinit var grantTypeAccess: String

    @Value("\${hh.grant_type_refresh}")
    private lateinit var grantTypeRefresh: String

    fun requestAccessToken(tgChatId: Long) {
        val secret = secretService.getUserSecretByTgId(tgChatId)
            ?: throw BotLogicException("Secret not found for user tgChatId $tgChatId")
        val code = secretService.getCode(tgChatId)
            ?: throw BotLogicException("Code not found for user tgChatId $tgChatId")
        val response = headHunterClient.requestToken(
            TokenRequest(
                grantType = grantTypeAccess,
                clientId = secret.clentId!!,
                clientSecret = secret.clientSecret!!,
                code = code,
                redirectUri = redirectUrl,
            )
        )
        if (response.statusCode.is2xxSuccessful) {
            val token = response.body!!
            tokenService.saveToken(token, tgChatId)
        } else {
            throw BotLogicException("Couldn't get token, got response code ${response.statusCode}: ${response.body}")
        }
    }

    fun refreshAccessToken(userId: Long, refreshToken: String): TokenDto {
        val secret = secretService.getUserSecretByUserId(userId)
            ?: throw BotLogicException("Secret not found for user id $userId")

        val response = headHunterClient.refreshToken(
            TokenRefreshRequest(
                grantType = grantTypeRefresh,
                clientId = secret.clentId!!,
                clientSecret = secret.clientSecret!!,
                refreshToken = refreshToken
            )
        )
        if (response.statusCode.is2xxSuccessful) {
            return response.body!!
        } else {
            throw BotLogicException("Couldn't refresh token, got response code ${response.statusCode}: ${response.body}")
        }
    }

}