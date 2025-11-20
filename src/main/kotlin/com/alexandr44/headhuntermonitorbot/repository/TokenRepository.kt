package com.alexandr44.headhuntermonitorbot.repository

import com.alexandr44.headhuntermonitorbot.entity.Token
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenRepository : JpaRepository<Token, Long> {

    fun findByUserId(userId: Long): Token

}
