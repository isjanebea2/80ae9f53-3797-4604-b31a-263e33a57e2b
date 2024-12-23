package com.example.desafio.domain.transaction

import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum
import java.math.BigDecimal

data class Withdraw(
    val status: TransactionCodesEnum,
    val totalAmount: BigDecimal,
    val accountAmount: AccountAmount,
    val merchantCategory: MerchantCategory,
    val pendingValue: BigDecimal
) {
    fun hasPendingAmount(): Boolean = pendingValue > BigDecimal.ZERO
}