package com.example.desafio.application.ports

import com.example.desafio.domain.transaction.WithdrawProcess

interface StatementHistoryService {
    fun withdrawSave(accountAmountId: Long, transaction: WithdrawProcess)
}