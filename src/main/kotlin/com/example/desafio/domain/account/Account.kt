package com.example.desafio.domain.account

import com.example.desafio.domain.transaction.Withdraw
import com.example.desafio.domain.transaction.WithdrawProcess
import com.example.desafio.domain.transaction.enums.MerchantCategory
import com.example.desafio.domain.transaction.enums.TransactionCodesEnum

data class Account(
    val availableAccountsAmount: HashMap<String, AccountAmount>,
    val id: Long = 0,
    val status: String
) {
    fun findAccountAmountByMcc(merchantCategory: MerchantCategory): AccountAmount? {
        return if (availableAccountsAmount.isNotEmpty() && availableAccountsAmount.containsKey(merchantCategory.internalName)) {
            availableAccountsAmount[merchantCategory.internalName]
        } else {
            null
        }
    }

    fun isAvailableAccountsAmount(): Boolean {
        return availableAccountsAmount.isEmpty()
    }
}