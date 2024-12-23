package com.example.desafio.application.ports.`in`

import com.example.desafio.domain.transaction.Transaction

interface StatementHistoryService {
    fun withdrawSave(accountAmountId: Long, transaction: Transaction)
}