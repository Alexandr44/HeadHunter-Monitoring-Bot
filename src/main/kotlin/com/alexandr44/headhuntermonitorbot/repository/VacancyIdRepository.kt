package com.alexandr44.headhuntermonitorbot.repository

import com.alexandr44.headhuntermonitorbot.entity.VacancyId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VacancyIdRepository : JpaRepository<VacancyId, Long> {

    fun findAllByUserId(userId: Long): List<VacancyId>

}
