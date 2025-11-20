package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.dto.response.TokenDto
import com.alexandr44.headhuntermonitorbot.entity.Token
import com.alexandr44.headhuntermonitorbot.repository.TokenRepository
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val tokenRepository: TokenRepository,
    private val userService: UserService
) {

    fun getTokenByTgId(tgChatId: Long): Token? {
        val user = userService.getUser(tgChatId)
        return tokenRepository.findByUserId(user.id!!)
    }

    fun saveToken(tokenDto: TokenDto, tgChatId: Long) {
        val user = userService.getUser(tgChatId)
        tokenRepository.save(
            Token(
                userId = user.id!!,
                accessToken = tokenDto.accessToken,
                refreshToken = tokenDto.refreshToken,
                expiredAt = System.currentTimeMillis() / 1000 + tokenDto.expiresIn
            )
        )
    }

}