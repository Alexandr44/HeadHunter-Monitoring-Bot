package com.alexandr44.headhuntermonitorbot.service

import com.alexandr44.headhuntermonitorbot.entity.Secret
import com.alexandr44.headhuntermonitorbot.repository.SecretRepository
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class SecretService(
    private val secretRepository: SecretRepository,
    private val userService: UserService
) {

    val codeMap = ConcurrentHashMap<Long, String>()

    companion object {
        const val AUTH_URL_PATTERN = "https://hh.ru/oauth/authorize?" +
                "response_type=code&" +
                "client_id=%s&" +
                "redirect_uri=https://hh.ru&" +
                "scope=read_profile,write_private_data"
    }

    fun getUserSecretByTgId(tgChatId: Long): Secret? {
        val user = userService.getUser(tgChatId)
        return secretRepository.findByUserId(user.id!!)
    }

    fun saveClientId(clientId: String, tgChatId: Long) {
        val user = userService.getUser(tgChatId)
        val secret = secretRepository.findByUserId(user.id!!)?.apply {
            this.clentId = clientId
        } ?: Secret(
            userId = user.id!!,
            clentId = clientId,
        )
        saveSecret(secret)
    }

    fun saveClientSecret(clientSecret: String, tgChatId: Long) {
        getUserSecretByTgId(tgChatId)!!.apply {
            this.clentId = clientSecret
            saveSecret(this)
        }
    }

    fun saveSecret(secret: Secret) {
        secretRepository.save(secret)
    }

    fun saveCode(code: String, tgChatId: Long) {
        codeMap[tgChatId] = code
    }

    fun getCode(tgChatId: Long): String {
        return codeMap.remove(tgChatId) ?: ""
    }
}