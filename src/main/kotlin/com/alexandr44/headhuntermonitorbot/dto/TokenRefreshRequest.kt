package com.alexandr44.headhuntermonitorbot.dto

import feign.Param

class TokenRefreshRequest(
    @get:Param("grant_type")
    val grantType: String,
    @get:Param("client_id")
    val clientId: String,
    @get:Param("client_secret")
    val clientSecret: String,
    @get:Param("refresh_token")
    val refreshToken: String
)
