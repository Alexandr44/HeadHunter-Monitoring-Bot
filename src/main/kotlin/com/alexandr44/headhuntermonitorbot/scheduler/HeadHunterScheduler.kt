package com.alexandr44.headhuntermonitorbot.scheduler

import com.alexandr44.headhuntermonitorbot.service.HeadHunterVacancyMonitorService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class HeadHunterScheduler(
    val headHunterVacancyMonitorService: HeadHunterVacancyMonitorService
) {

    @Scheduled(cron = "\${scheduler.cron}")
    fun schedule() {
        headHunterVacancyMonitorService.checkVacancies()
    }

    @Scheduled(cron = "\${scheduler.refresh-cron}")
    fun scheduleRefreshToken() {
        headHunterVacancyMonitorService.refreshTokens()
    }

}