package com.example.desafio.domain.transaction

import com.example.desafio.domain.account.AccountAmount
import com.example.desafio.domain.transaction.enums.Mcc
import com.example.desafio.domain.transaction.enums.TransactionCode
import java.math.BigDecimal

data class WithdrawProcess(
    val availableAccounts: HashMap<String, AccountAmount>,
    val totalWithdrawalAmount: BigDecimal,
    val mcc: Mcc,
    val merchant: String,
) {

    fun toResult(): Withdraw {
        val withdraw = Withdraw(totalAmount = totalWithdrawalAmount, mcc = mcc);

        if (availableAccounts.isEmpty() || !availableAccounts.containsKey(mcc.internalName)) {
            return withdraw.copy(status = TransactionCode.INTERNAL_ERROR)
        }

        if (authorize(mcc.internalName)) {
            return withdraw.copy(
                status = TransactionCode.AVAILABLE,
                accountAmountTargetId = availableAccounts.getValue(mcc.internalName).id
            )
        }

        return withdraw.copy(status = TransactionCode.REJECT)
    }

    private fun authorize(currentMcc: String): Boolean {
        if (!availableAccounts.containsKey(currentMcc)) {
            return false
        }

        val currentAmount = availableAccounts.getValue(currentMcc).value

        return (currentAmount - totalWithdrawalAmount) > BigDecimal.ZERO
    }

}