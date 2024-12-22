package com.example.desafio.adapters.jpa_database.repository

import com.example.desafio.adapters.jpa_database.entity.AccountAmountEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AccountAmountRepository : JpaRepository<AccountAmountEntity, Long> {
    fun findByAccountId(accountId: Long): MutableList<AccountAmountEntity>
}