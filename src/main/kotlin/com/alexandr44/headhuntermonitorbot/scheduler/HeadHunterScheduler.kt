package com.alexandr44.headhuntermonitorbot.scheduler

import com.alexandr44.headhuntermonitorbot.service.HeadHunterService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class HeadHunterScheduler(
    val headHunterService: HeadHunterService
) {

    @Scheduled(cron = "\${scheduler.cron}")
    fun schedule() {
        headHunterService.checkVacancies()
    }

}