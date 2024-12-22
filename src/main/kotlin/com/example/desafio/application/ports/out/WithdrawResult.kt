package com.example.desafio.application.ports.out

import com.example.desafio.domain.transaction.enums.TransactionCodesEnum

data class WithdrawResult(
    val status: TransactionCodesEnum
)