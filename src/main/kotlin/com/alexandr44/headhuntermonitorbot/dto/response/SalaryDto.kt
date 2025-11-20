package com.alexandr44.headhuntermonitorbot.dto.response

data class SalaryDto(
    val from: Int,
    val to: Int,
    val currency: String,
    val amount: Int? = null,
)
