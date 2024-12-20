package com.example.desafio.adapters.jpa_database.repository

import com.example.desafio.domain.account.AccountAmount
import org.springframework.data.jpa.repository.JpaRepository

interface AccountAmountRepository : JpaRepository<AccountAmount, Long> {
    fun findByAccountId(accountId: Long): MutableList<AccountAmount>
}