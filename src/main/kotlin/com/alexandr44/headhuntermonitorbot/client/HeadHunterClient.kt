package com.alexandr44.headhuntermonitorbot.client

import com.alexandr44.headhuntermonitorbot.dto.VacancyResponseDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
    name = "my-service",
    url = "https://api.hh.ru"
)
interface HeadHunterClient {

    @GetMapping("/vacancies")
    fun getVacancies(
        @RequestParam("text") text: String,
        @RequestParam("schedule") schedule: String,
        @RequestParam("order_by") orderBy: String,
        @RequestParam("per_page") perPage: Int,
        @RequestParam("page") page: Int
    ): ResponseEntity<VacancyResponseDto>

}