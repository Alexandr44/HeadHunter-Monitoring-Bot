package com.alexandr44.headhuntermonitorbot.client

import com.alexandr44.headhuntermonitorbot.dto.TokenRefreshRequest
import com.alexandr44.headhuntermonitorbot.dto.TokenRequest
import com.alexandr44.headhuntermonitorbot.dto.response.ResumeDto
import com.alexandr44.headhuntermonitorbot.dto.response.TokenDto
import com.alexandr44.headhuntermonitorbot.dto.response.VacancyResponseDto
import feign.Headers
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@FeignClient(
    name = "my-service",
    url = "https://api.hh.ru",
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

    @PostMapping(
        value = ["/token"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    @Headers("${HttpHeaders.CONTENT_TYPE}: ${MediaType.APPLICATION_FORM_URLENCODED_VALUE}")
    fun requestToken(@SpringQueryMap body: TokenRequest): ResponseEntity<TokenDto>

    @PostMapping("/token")
    @Headers("${HttpHeaders.CONTENT_TYPE}: ${MediaType.APPLICATION_FORM_URLENCODED_VALUE}")
    fun refreshToken(@SpringQueryMap body: TokenRefreshRequest): ResponseEntity<TokenDto>

    @GetMapping("/resumes/{resumeId}")
    fun getResumeById(
        @PathVariable("resumeId") resumeId: String,
        @RequestHeader("Authorization") bearerToken: String
    ): ResponseEntity<ResumeDto>

}