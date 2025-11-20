package com.alexandr44.headhuntermonitorbot.repository

import com.alexandr44.headhuntermonitorbot.entity.Secret
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SecretRepository : JpaRepository<Secret, Long> {

    fun findByUserId(userId: Long): Secret?

}
