package com.example.desafio.application.ports.`in`

import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.Withdraw

interface StatementHistoryService {
    fun withdrawSave(accountAmountId: Long, transaction: Transaction)
    fun withdrawSave(transaction: Transaction, withdrawList: List<Withdraw>)
}