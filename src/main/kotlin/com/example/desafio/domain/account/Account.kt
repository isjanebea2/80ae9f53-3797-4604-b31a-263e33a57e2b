package com.example.desafio.domain.account

import com.example.desafio.domain.transaction.enums.MerchantCategory
import java.math.BigDecimal

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

    fun notAvailableAccountsAmount(): Boolean {
        return availableAccountsAmount.isEmpty()
    }

    private fun calculateTotalAmount() = availableAccountsAmount.values
        .map { it.value }
        .reduce { acc, balance -> acc + balance }

    fun isAvailableBalance(totalAmount: BigDecimal): Boolean {
        return calculateTotalAmount() >= totalAmount
    }
}