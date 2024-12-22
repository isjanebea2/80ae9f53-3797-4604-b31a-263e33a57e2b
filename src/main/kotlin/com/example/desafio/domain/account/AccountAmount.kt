package com.example.desafio.domain.account

import com.example.desafio.adapters.jpa_database.entity.AccountTypeEntity
import java.math.BigDecimal

data class AccountAmount(
    val id: Long,

    val value: BigDecimal,

    val accountTypeEntity: AccountTypeEntity,

    ) {
    fun withdraw(amount: BigDecimal) = this.copy(value = (value - amount))

    fun authorizeWithdraw(amount: BigDecimal) = value >= amount
}