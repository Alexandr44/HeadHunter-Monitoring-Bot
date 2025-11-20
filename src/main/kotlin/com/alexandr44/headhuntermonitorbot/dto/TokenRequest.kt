package com.alexandr44.headhuntermonitorbot.dto

import com.fasterxml.jackson.annotation.JsonProperty
import feign.Param

class TokenRequest(
    @get:Param("grant_type")
    val grantType: String,
    @get:Param("client_id")
    val clientId: String,
    @get:Param("client_secret")
    val clientSecret: String,
    val code: String,
    @get:Param("redirect_uri")
    val redirectUri: String



)