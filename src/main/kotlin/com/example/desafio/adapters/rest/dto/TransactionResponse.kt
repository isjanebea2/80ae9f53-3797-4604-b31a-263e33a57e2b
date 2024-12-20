package com.example.desafio.adapters.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionResponse(
    @JsonProperty("code")
    val code: String
)