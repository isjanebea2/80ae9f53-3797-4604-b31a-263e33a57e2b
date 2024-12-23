package com.example.desafio.adapters.rest.dto

import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionRequest(
    val mcc: String,
    @JsonProperty("account") val accountId: String,
    val totalAmount: Double,
    val merchant: String,
) {
    fun toDomain() = Transaction(
        totalWithdrawalAmount = totalAmount.toBigDecimal(),
        merchantCategory = MerchantCategory.fromString(mcc),
        merchant = merchant,
        accountId = accountId,
        status = TransactionCodesEnum.REJECT
    )
}