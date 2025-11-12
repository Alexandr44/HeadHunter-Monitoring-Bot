package com.alexandr44.headhuntermonitorbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HeadHunterMonitorBotApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<HeadHunterMonitorBotApplication>(*args)
        }
    }
}