package com.example.desafio.domain.transaction

import com.example.desafio.domain.transaction.enums.Mcc
import com.example.desafio.domain.transaction.enums.TransactionCode
import java.math.BigDecimal

data class Withdraw(
    val status: TransactionCode = TransactionCode.REJECT,
    val totalAmount: BigDecimal = BigDecimal.ZERO,
    val accountAmountTargetId: Long = 0,
    val mcc: Mcc,
) {
    fun isRejected(): Boolean {
        return status.isRejected()
    }

    fun isError(): Boolean {
        return status.isError()
    }
}
