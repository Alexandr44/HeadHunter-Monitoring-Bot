package com.alexandr44.headhuntermonitorbot.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class ResumeDto(
    val id: String,
    val title: String,
    @JsonProperty("first_name")
    val firstName: String?,
    @JsonProperty("last_name")
    val lastName: String?,
    @JsonProperty("middle_name")
    val middleName: String?,
    val age: Int?,
    val gender: GenderDto?,
    val area: AreaDto?,
    val salary: SalaryDto?,
    val skills: String?,
    val experience: List<ExperienceItemDto>?,
    val contact: List<ContactDto>?,
    @JsonProperty("alternate_url")
    val alternateUrl: String?
)
