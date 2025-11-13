package com.alexandr44.headhuntermonitorbot.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class VacancyDto(
    val id: Long,
    val name: String,
    @JsonProperty("published_at")
    val publishedAt: OffsetDateTime,
    @JsonProperty("alternate_url")
    val alternateUrl: String,
    val employer: EmployerDto,
    val salary: SalaryDto?,
    @JsonProperty("salary_range")
    val salaryRange: SalaryDto?,
)
