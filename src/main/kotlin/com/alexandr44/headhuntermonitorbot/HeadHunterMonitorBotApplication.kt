package com.alexandr44.headhuntermonitorbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableJpaAuditing
@EnableFeignClients
@SpringBootApplication
class HeadHunterMonitorBotApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<HeadHunterMonitorBotApplication>(*args)
        }
    }
}