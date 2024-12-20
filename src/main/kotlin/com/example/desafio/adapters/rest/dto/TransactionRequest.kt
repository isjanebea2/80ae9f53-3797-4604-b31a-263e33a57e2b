package com.example.desafio.adapters.rest.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionRequest(
    val mcc: String,
    @JsonProperty("account") val accountId: String,
    val totalAmount: Double,
    val merchant: String,
)