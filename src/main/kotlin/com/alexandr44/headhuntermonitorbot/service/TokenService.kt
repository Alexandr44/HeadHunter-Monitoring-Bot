package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.dto.response.TokenDto
import com.alexandr44.headhuntermonitorbot.entity.Token
import com.alexandr44.headhuntermonitorbot.repository.TokenRepository
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val tokenRepository: TokenRepository,
    private val userService: UserService
) {

    fun getTokenByTgId(tgChatId: Long): Token? {
        val user = userService.getUser(tgChatId)!!
        return tokenRepository.findByUserId(user.id!!)
    }

    fun saveToken(tokenDto: TokenDto, tgChatId: Long) {
        val token: Token = getTokenByTgId(tgChatId) ?: runBlocking {
            val user = userService.getUser(tgChatId)!!
            return@runBlocking Token(
                userId = user.id!!
            )
        }.let {
            it.accessToken = tokenDto.accessToken
            it.refreshToken = tokenDto.refreshToken
            it.expiredAt = System.currentTimeMillis() / 1000 + tokenDto.expiresIn
            return@let it
        }
        tokenRepository.save(token)
    }

}