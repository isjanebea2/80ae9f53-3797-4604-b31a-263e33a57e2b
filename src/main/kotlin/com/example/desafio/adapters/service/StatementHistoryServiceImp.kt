package com.example.desafio.adapters.service

import com.example.desafio.adapters.jpa_database.repository.StatementOutputRepository
import com.example.desafio.application.ports.`in`.StatementHistoryService
import com.example.desafio.adapters.jpa_database.entity.StatementHistory
import com.example.desafio.domain.transaction.Transaction
import com.example.desafio.domain.transaction.Withdraw
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class StatementHistoryServiceImp(
    private val statementOutputRepository: StatementOutputRepository,
) : StatementHistoryService {
    override fun withdrawSave(accountAmountId: Long, transaction: Transaction) {
        this.statementOutputRepository.customInsert(
           id = UUID.randomUUID().toString(),
            value = transaction.totalWithdrawalAmount,
            merchant = transaction.merchant,
            accountAmountId = accountAmountId,
            transactionLogId = transaction.id.toString(),
            updatedAt = Instant.now(),
            createdAt = Instant.now(),
        )
    }

    override fun withdrawSave(transaction: Transaction, withdrawList: List<Withdraw>) {
        val pendingList: List<StatementHistory> = withdrawList.map {
            StatementHistory(
                merchant = transaction.merchant,
                value = it.totalAmount,
                accountAmountId = it.accountAmount.id,
                transactionLogId = transaction.id.toString(),
            )
        }

        pendingList.forEach {
            this.statementOutputRepository.customInsert(
                UUID.randomUUID().toString(), it.value, it.merchant, it.accountAmountId, it.transactionLogId, Instant.now(), Instant.now()
            )
        }
    }
}