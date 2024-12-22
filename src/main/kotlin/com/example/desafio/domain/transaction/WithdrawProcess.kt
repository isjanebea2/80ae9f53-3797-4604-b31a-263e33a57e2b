package com.example.desafio.domain.transaction

import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import java.math.BigDecimal

data class WithdrawProcess(
    val totalWithdrawalAmount: BigDecimal,
    val merchantCategory: MerchantCategory,
    val merchant: String,
    val accountId: String,
    var status: TransactionCodesEnum,
) {
    fun changeStatus(newStatus: TransactionCodesEnum): Unit {
        status = newStatus
    }
}