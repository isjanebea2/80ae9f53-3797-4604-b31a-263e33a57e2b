package com.example.desafio.domain.account

import com.example.desafio.adapters.jpa_database.entity.AccountTypeEntity
import com.example.desafio.domain.transaction.Withdraw
import java.math.BigDecimal

data class AccountAmount(
    val id: Long,

    val value: BigDecimal,

    val accountTypeEntity: AccountTypeEntity,

    ) {
    fun authorizeWithdraw(amount: BigDecimal): Boolean {
         return if (amount == value) true
         else if (value - amount >= BigDecimal.ZERO) true
         else  false
    }

    fun toPendingBalance(totalAmount: BigDecimal): BigDecimal {
        return if (authorizeWithdraw(totalAmount)) {
            BigDecimal.ZERO
        } else {
            totalAmount - value
        }
    }

    fun toWithdraw(totalAmount: BigDecimal): BigDecimal {
        return  value - totalAmount
    }


 }