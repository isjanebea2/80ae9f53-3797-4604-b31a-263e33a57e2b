package com.example.desafio.domain.transaction

import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import java.math.BigDecimal
import java.util.*

data class Transaction(
    val totalWithdrawalAmount: BigDecimal,
    val merchantCategory: MerchantCategory,
    val merchant: String,
    val accountId: String,
    var status: TransactionCodesEnum,
    val id: UUID = UUID.randomUUID(),
) {
    fun changeStatus(newStatus: TransactionCodesEnum): Unit {
        status = newStatus
    }
}