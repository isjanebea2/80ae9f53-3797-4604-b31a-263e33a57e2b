package com.example.desafio.adapters.service

import com.example.desafio.adapters.jpa_database.repository.StatementOutputRepository
import com.example.desafio.application.ports.`in`.StatementHistoryService
import com.example.desafio.domain.transaction.StatementHistory
import com.example.desafio.domain.transaction.WithdrawProcess
import org.springframework.stereotype.Service

@Service
class StatementHistoryServiceImp(
    private val statementOutputRepository: StatementOutputRepository,
) : StatementHistoryService {
    override fun withdrawSave(accountAmountId: Long, transaction: WithdrawProcess) {
        this.statementOutputRepository.save(
            StatementHistory(
                merchant = transaction.merchant,
                value = transaction.totalWithdrawalAmount,
                accountAmountId = accountAmountId
            )
        )
    }
}