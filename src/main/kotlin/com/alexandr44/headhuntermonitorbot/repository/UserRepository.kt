package com.alexandr44.headhuntermonitorbot.repository

import com.alexandr44.headhuntermonitorbot.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findAllByActiveIsTrue(): List<User>

    fun findByUserChatId(chatId: Long): User?

}
